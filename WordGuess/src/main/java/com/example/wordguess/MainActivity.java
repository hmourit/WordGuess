package com.example.wordguess;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class MainActivity extends Activity {

    TextView tvDefinition;
    EditText etWord;
    Button butSend;

    Resources res;

    Iterator<Integer> letterIds;
    boolean debug = false;
    Word currentWord;
    int activeLetterId;

    ProgressDialog  progress;
    CreateGameAsync task;

    //Map saving for each letter a Word and a Boolean indicating whether the word has been guessed(true) or not(false).
    //If the word has never been accessed then this Boolean would be null.
    HashMap<String,Pair<Word, Boolean>> wordState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadUI();
        setCallbacks();

        res = getResources();

        wordState = new HashMap<>();

        letterIds = Utils.getCycleIterator(getResources().obtainTypedArray(R.array.button_ids));

        if (savedInstanceState != null){
            wordState = (HashMap)savedInstanceState.getSerializable(res.getString(R.string.state_map));
            activeLetterId = savedInstanceState.getInt(res.getString(R.string.activeLetter));
            debug = savedInstanceState.getBoolean(res.getString(R.string.debug));
            setBackgroundColors();
            loadWordAndDefinition((TextView) findViewById(activeLetterId));
        }else{
            activeLetterId = letterIds.next();
            progress = new ProgressDialog(this);
            task = new CreateGameAsync();
            task.execute();
        }
        if (debug){
            activateDebug();
        }

    }

    /**
     * Load UI elements.
     */
    private void loadUI() {
        tvDefinition = (TextView) findViewById(R.id.tv_definition);
        etWord = (EditText) findViewById(R.id.et_word);
        butSend = (Button) findViewById(R.id.but_send);
    }

    /**
     * Set callbacks for UI elements.
     */
    private void setCallbacks() {

        etWord.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!debug) {
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
                // checks introduced word and load next one
                TextView activeTextViewLetter = (TextView) findViewById(activeLetterId);
                if (currentWord.getName().toLowerCase().equals(etWord.getText().toString().trim().toLowerCase())) {
                    activeTextViewLetter.setBackgroundColor(Color.GREEN);
                    wordState.put(currentWord.getStarts_with(), new Pair<>(currentWord, true));
                } else {
                    activeTextViewLetter.setBackgroundColor(Color.RED);
                    wordState.put(currentWord.getStarts_with(), new Pair<>(currentWord, false));
                }
                etWord.setText("");
                activeLetterId = letterIds.next();
                loadWordAndDefinition((TextView) findViewById(activeLetterId));
            }
        });
    }

    /**
     * Activates debugging mode, i.e., shows desired word as hint and makes letters clickable.
     */
    private void activateDebug() {

        res = getResources();
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
        debug = true;
    }

    /**
     * Deactivates debugging mode, i.e., hides hint and removes onClick callbacks.
     */
    private void deactivateDebug() {

        res = getResources();
        TypedArray buttonIds = res.obtainTypedArray(R.array.button_ids);
        for (int i = 0; i < buttonIds.length(); i++) {
            int butLetter = buttonIds.getResourceId(i, 0);
            findViewById(butLetter).setOnClickListener(null);
        }

        etWord.setHint(R.string.word);
        debug = false;
    }

    /**
     * Loads definition for a word starting with a letter.
     */
    private void loadWordAndDefinition(TextView textView) {
        /*String startsWith = textView.getText().toString().toLowerCase();
        Word w = DBAccess.getRWordStarting(startsWith, res);
        if (w != null) {
            currentWord = w.getName().toLowerCase();
            tvDefinition.setText(w.getRandomDefinition());
            if (debug) {
                etWord.setHint(w.getName());
            }
        } else {
            // TODO: react to error
        }*/
        String startsWith = textView.getText().toString().toLowerCase();
        Pair<Word, Boolean> wordDef = wordState.get(startsWith);
        currentWord = wordDef.first;
        tvDefinition.setText(wordDef.first.getActive_def());
        if (debug) {
            etWord.setHint(wordDef.first.getName());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(res.getString(R.string.state_map),wordState);
        outState.putInt(res.getString(R.string.activeLetter), activeLetterId);
        outState.putBoolean(res.getString(R.string.debug), debug);
    }

    /**
     * Sets the textview background color to RED if a word was guessed wrongly or to GREEN otherwise.
     * Called after restoring the state of the application.
     */

    private void setBackgroundColors(){
        TypedArray buttonIds = res.obtainTypedArray(R.array.button_ids);
        for (int i = 0; i < buttonIds.length(); i++) {
            int butLetter = buttonIds.getResourceId(i, 0);
            TextView tv = (TextView) findViewById(butLetter);
            Pair<Word, Boolean> content = wordState.get(tv.getText().toString().toLowerCase());
            if (content.second != null){
                if (content.second.booleanValue()){
                    tv.setBackgroundColor(Color.GREEN);
                }else{
                    tv.setBackgroundColor(Color.RED);
                }
            }

        }
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

            for(int i = 0; i<letters.length; i++){
                String letter = letters[i].toLowerCase();
                Word w = DBAccess.getRWordStarting(letter, res);
                w.setActive_def();
                if(w != null){
                    wordState.put(letter, new Pair<Word,Boolean>(w, null));
                }else{
                    // TODO: react to error
                }
                publishProgress("Creando palabra empezando por: " + letter, String.valueOf(i+1));
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
        }
    }



}
