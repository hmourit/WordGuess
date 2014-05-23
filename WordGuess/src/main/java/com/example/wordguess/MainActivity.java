package com.example.wordguess;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;


public class MainActivity extends Activity {

    TextView tvDefinition;
    EditText etWord;
    Button butSend;
    Button butNext;

    // countdown
    TextView tvCountdown;
    long timeout = 60000l; // millis
    long interval = 500l; // millis, with 1000 it's not precise
    CountDownTimer countDownTimer;
    boolean countDownPaused = false;

    Resources res;

    Iterator<Integer> letterIds;
    boolean debugMode = false;
    Word currentWord;
    int activeLetterId;

    ProgressDialog progress;
    CreateGameAsync task;

    // Map saving for each letter a Word and an Integer indicating whether the word has been
    // guessed (1) or not (0).
    // If the word has never been accessed then this int would be 2.
    HashMap<String, Pair<Word, Integer>> wordState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadUI();
        setCallbacks();

        res = getResources();
        wordState = new HashMap<>();
        letterIds = Utils.getCycleIterator(getResources().obtainTypedArray(R.array.button_ids));

        if (savedInstanceState != null) {
            restorePreviousState(savedInstanceState);
        } else {
            createNewGame();
        }
        if (debugMode) {
            activateDebug();
        }

    }

    @Override
    protected void onPause() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        super.onPause();

    }

    @Override
    protected void onResume() {
        if (countDownPaused) {
            startCountdown(timeout);
            countDownPaused = false;
        }
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(res.getString(R.string.state_map), wordState);
        outState.putInt(res.getString(R.string.activeLetter), activeLetterId);
        outState.putBoolean(res.getString(R.string.debug), debugMode);
    }

    /**
     * Retrieves words for a new game.
     */
    private void createNewGame() {
        activeLetterId = letterIds.next();
        progress = new ProgressDialog(this);
        task = new CreateGameAsync();
        task.execute();
    }

    /**
     * Restores state from a previous, saved one.
     *
     * @param savedInstanceState
     */
    private void restorePreviousState(Bundle savedInstanceState) {
        wordState = (HashMap) savedInstanceState
                .getSerializable(res.getString(R.string.state_map));
        activeLetterId = savedInstanceState.getInt(res.getString(R.string.activeLetter));
        debugMode = savedInstanceState.getBoolean(res.getString(R.string.debug));
        setBackgroundColors();
        loadWordAndDefinition((TextView) findViewById(activeLetterId));
    }

    /**
     * Starts countdown.
     *
     * @param millisInFuture
     */
    private void startCountdown(long millisInFuture) {

        countDownTimer = new CountDownTimer(millisInFuture, interval) {
            public void onTick(long millisUntilFinished) {
                updateCountdown(millisUntilFinished);
            }

            public void onFinish() {
                // TODO: Game over screen.
                updateCountdown(0l);
            }
        }.start();

    }

    /**
     * Updates countdown.
     */
    private void updateCountdown(long millisUntilFinished) {

        timeout = millisUntilFinished;
        if (millisUntilFinished <= 10000l) {
            tvCountdown.setTextColor(Color.RED);
        }
        if (millisUntilFinished > 0) {
            tvCountdown.setText(String.valueOf(timeout / 1000));
        } else {
            tvCountdown.setText("0");
            tvDefinition.setText("");
            butSend.setEnabled(false);
            butNext.setEnabled(false);
            etWord.setEnabled(false);
        }

    }

    /**
     * Load UI elements.
     */
    private void loadUI() {

        tvDefinition = (TextView) findViewById(R.id.tv_definition);
        etWord = (EditText) findViewById(R.id.et_word);
        butSend = (Button) findViewById(R.id.but_send);
        butNext = (Button) findViewById(R.id.but_next);
        tvCountdown = (TextView) findViewById(R.id.tv_countdown);

    }

    /**
     * Set callbacks for UI elements.
     */
    private void setCallbacks() {

        etWord.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!debugMode) {
                    activateDebug();
                } else {
                    deactivateDebug();
                }
                return true;
            }
        });


        butSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // checks introduced word and loads next one
                TextView activeTextViewLetter = (TextView) findViewById(activeLetterId);
                if (currentWord.getName().toLowerCase().equals(etWord.getText().toString().trim().toLowerCase())) {
                    activeTextViewLetter.setBackgroundColor(Color.GREEN);
                    wordState.put(currentWord.getStartsWith(), new Pair<>(currentWord, 1));
                } else {
                    activeTextViewLetter.setBackgroundColor(Color.RED);
                    wordState.put(currentWord.getStartsWith(), new Pair<>(currentWord, 0));
                }
                nextWord();
            }
        });

        butNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextWord();
            }
        });

    }

    /**
     * Loads word for next letter and cleans etWord.
     */
    private void nextWord() {
        TextView activeTextViewLetter = (TextView) findViewById(activeLetterId);
        activeTextViewLetter.setTextAppearance(this.getApplicationContext(), android.R.style.TextAppearance_Medium);
        activeTextViewLetter.setTypeface(activeTextViewLetter.getTypeface(), Typeface.BOLD);
        activeTextViewLetter.setTextColor(Color.BLACK);
        etWord.setText("");
        activeLetterId = letterIds.next();
        loadWordAndDefinition((TextView) findViewById(activeLetterId));

    }

    /**
     * Activates debugging mode, i.e., shows desired word as hint and makes letters clickable.
     */
    private void activateDebug() {

        TypedArray buttonIds = res.obtainTypedArray(R.array.button_ids);
        for (int i = 0; i < buttonIds.length(); i++) {
            final int butLetter = buttonIds.getResourceId(i, 0);
            findViewById(butLetter).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activeLetterId = butLetter;
                    loadWordAndDefinition((TextView) view);
                }
            });
        }

        if (!currentWord.getName().equals("")) {
            etWord.setHint(currentWord.getName());
        }
        debugMode = true;
    }

    /**
     * Deactivates debugging mode, i.e., hides hint and removes onClick callbacks.
     */
    private void deactivateDebug() {

        TypedArray buttonIds = res.obtainTypedArray(R.array.button_ids);
        for (int i = 0; i < buttonIds.length(); i++) {
            int butLetter = buttonIds.getResourceId(i, 0);
            findViewById(butLetter).setOnClickListener(null);
        }

        etWord.setHint(R.string.word);
        debugMode = false;
    }

    /**
     * Loads definition for a word starting with a letter.
     */
    private void loadWordAndDefinition(TextView textView) {
        textView.setTextAppearance(this.getApplicationContext(),android.R.style.TextAppearance_Large);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextColor(Color.rgb(4, 189, 218));
        String startsWith = textView.getText().toString().toLowerCase();
        Pair<Word, Integer> wordDef = wordState.get(startsWith);
        currentWord = wordDef.getFirst();
        tvDefinition.setText(wordDef.getFirst().getActiveDef());
        if (debugMode) {
            etWord.setHint(wordDef.getFirst().getName());
        }
    }

    /**
     * Sets the textview background color to RED if a word was guessed wrongly or to GREEN otherwise.
     * Called after restoring the state of the application.
     */

    private void setBackgroundColors() {
        TypedArray buttonIds = res.obtainTypedArray(R.array.button_ids);
        for (int i = 0; i < buttonIds.length(); i++) {
            int butLetter = buttonIds.getResourceId(i, 0);
            TextView tv = (TextView) findViewById(butLetter);
            Pair<Word, Integer> content = wordState.get(tv.getText().toString().toLowerCase());
            switch (content.getSecond()) {
                case 0:
                    tv.setBackgroundColor(Color.RED);
                    break;
                case 1:
                    tv.setBackgroundColor(Color.GREEN);
                    break;
            }
        }
    }

    private class CreateGameAsync extends AsyncTask<Void, String, Void> {

        @Override
        protected void onPreExecute() {
            progress.setMax(res.getIntArray(R.array.spanish_alphabet).length);
            progress.setMessage("Creando partida");
            progress.setTitle("WordGuess");
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String[] letters = res.getStringArray(R.array.spanish_alphabet);

            for (int i = 0; i < letters.length; i++) {
                String letter = letters[i].toLowerCase();
                Word w = DBAccess.getRWordStarting(letter, res);
                if (w != null) {
                    w.setActiveDef();
                    wordState.put(letter, new Pair<Word, Integer>(w, 2));
                } else {
                    // TODO: react to error
                }
                publishProgress("Creando palabra empezando por: " + letter, String.valueOf(i + 1));
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            progress.setProgress(Integer.parseInt(values[1]));
            progress.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progress.cancel();
            loadWordAndDefinition((TextView) findViewById(activeLetterId));
            startCountdown(timeout);
        }
    }


}
