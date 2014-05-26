package com.example.wordguess;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class GameActivity extends Activity {

    TextView tvDefinition;
    EditText etWord;
    Button butSend;
    Button butNext;

    // countdown
    TextView tvCountdown;
    long remainingTime; // millis
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
        setContentView(R.layout.activity_game);

        loadUI();
        setCallbacks();

        res = getResources();
        wordState = new HashMap<>();
        letterIds = Utils.getCycleIterator(getResources().obtainTypedArray(R.array.button_ids));

        if (savedInstanceState != null) {
            restorePreviousState(savedInstanceState);
        } else {
            Intent sender=getIntent();
            if(sender != null && sender.getAction().equals(res.getString(R.string.continue_game_intent_action))){
                restoreGame();
                setBackgroundColors();
                loadWordAndDefinition((TextView) findViewById(activeLetterId));
                startCountdown(remainingTime);
            }else{
                createNewGame();
            }
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
            countDownPaused = true;
        }
        super.onPause();

    }

    @Override
    protected void onResume() {
        if (countDownPaused) {
            startCountdown(remainingTime);
            countDownPaused = false;
        }
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game_settings, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_save) {
            while(wordState.size() < res.getStringArray(R.array.spanish_alphabet).length){
                //Wait for all words to load
            }
            JSONObject obj = new JSONObject();
            try {
                String[] keys = new String[res.getStringArray(R.array.spanish_alphabet).length];
                wordState.keySet().toArray(keys);
                JSONArray words = new JSONArray();
                for(String key: keys){
                    Pair<Word, Integer> p = wordState.get(key);
                    JSONObject pair = new JSONObject();
                    pair.put(res.getString(R.string.save_pair_json_first), p.getFirst().toJsonObject(res));
                    pair.put(res.getString(R.string.save_pair_json_second),p.getSecond());
                    words.put(pair);
                }
                obj.put(res.getString(R.string.save_json_state_map), words);
                obj.put(res.getString(R.string.save_time_json), remainingTime);
                obj.put(res.getString(R.string.save_active_word), activeLetterId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String FILENAME = getResources().getString(R.string.save_file) + ".json";
            FileOutputStream fos = null;
            try {
                fos = openFileOutput(FILENAME,
                        Context.MODE_WORLD_READABLE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fos.write(obj.toString().getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private void restoreGame(){
        String text = "";
        try {
            String filename = res.getString(R.string.save_file) + ".json";
            FileInputStream inputStream = openFileInput(filename);
            text = Utils.fromInputStreamToString(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonObject = (JSONObject) new JSONTokener(text).nextValue();
            remainingTime = jsonObject.getInt(res.getString(R.string.save_time_json));
            activeLetterId = jsonObject.getInt(res.getString(R.string.save_active_word));
            JSONArray words = jsonObject.getJSONArray(res.getString(R.string.save_json_state_map));
            List<JSONObject> list = new ArrayList<JSONObject>();
            for (int i = 0; i < words.length(); i++) {
                list.add(words.getJSONObject(i));
            }

            for(JSONObject o: list){
                JSONObject object = o.getJSONObject(res.getString(R.string.save_pair_json_first));
                String starts = object.getString(res.getString(R.string.save_starts_word_json));
                int state = o.getInt(res.getString(R.string.save_pair_json_second));
                wordState.put(starts,new Pair<Word, Integer>(new Word(object,res),state));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(getString(R.string.state_map), wordState);
        outState.putInt(getString(R.string.activeLetter), activeLetterId);
        outState.putBoolean(getString(R.string.debug), debugMode);
        outState.putLong(getString(R.string.remaining_key), remainingTime);
    }

    /**
     * Retrieves words for a new game.
     */
    private void createNewGame() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String timeoutStr = sharedPreferences.getString(
                getString(R.string.pref_timeout_key),
                getString(R.string.pref_timeout_default_str));
        remainingTime = Long.valueOf(timeoutStr) * 1000;

        activeLetterId = letterIds.next();
        progress = new ProgressDialog(this);
        String prechargedWordsStr = sharedPreferences.getString(
                getString(R.string.pref_precharged_words_key),
                getString(R.string.pref_precharged_words_default));
        task = new CreateGameAsync(Integer.valueOf(prechargedWordsStr));
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
        remainingTime = savedInstanceState.getLong(getString(R.string.remaining_key));
        setBackgroundColors();
        loadWordAndDefinition((TextView) findViewById(activeLetterId));
        startCountdown(remainingTime);

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

        remainingTime = millisUntilFinished;
        if (millisUntilFinished <= 10000l) {
            tvCountdown.setTextColor(Color.RED);
        }
        if (millisUntilFinished > 0) {
            tvCountdown.setText(String.valueOf(remainingTime / 1000));
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
        textView.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Large);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextColor(Color.rgb(4, 189, 218));
        String startsWith = textView.getText().toString().toLowerCase();
        while (!wordState.containsKey(startsWith)) {
        }
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

        private int wordBuffer = 1;

        CreateGameAsync(int wb) {
            if (wb > 0 || wb <= getResources().getStringArray(R.array.spanish_alphabet).length) {
                wordBuffer = wb;
            }
        }


        @Override
        protected void onPreExecute() {
            progress.setMax(wordBuffer);
            progress.setMessage(getString(R.string.creating_game));
            progress.setTitle(getString(R.string.app_name));
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String[] letters = res.getStringArray(R.array.spanish_alphabet);

            for (int i = 0; i < letters.length; i++) {
                if (i == wordBuffer) {
                    progress.dismiss();
                }
                String letter = letters[i].toLowerCase();
                Word w = DBAccess.getRWordStarting(letter, res);
                if (w != null) {
                    w.setActiveDef();
                    wordState.put(letter, new Pair<Word, Integer>(w, 2));
                } else {
                    // TODO: react to error
                }
                publishProgress(getString(R.string.looking_for_word) + " " + letter.toUpperCase(),
                        String.valueOf(i + 1));
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (progress != null) {
                progress.setProgress(Integer.parseInt(values[1]));
                progress.setMessage(values[0]);
            }
            if (Integer.parseInt(values[1]) == wordBuffer) {
                loadWordAndDefinition((TextView) findViewById(activeLetterId));
                startCountdown(remainingTime);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (progress != null) {
                progress.cancel();
            }
        }
    }


}
