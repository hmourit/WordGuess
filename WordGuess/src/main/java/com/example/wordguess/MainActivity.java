package com.example.wordguess;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Iterator;


public class MainActivity extends Activity {

    TextView tvDefinition;
    EditText etWord;
    Button butSend;

    Resources res;

    Iterator<Integer> letterIds;
    boolean debug = false;
    String currentWord = "";
    int activeLetterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadUI();
        setCallbacks();

        res = getResources();

        letterIds = Utils.getCycleIterator(getResources().obtainTypedArray(R.array.button_ids));
        activeLetterId = letterIds.next();
        loadWordAndDefinition((TextView) findViewById(activeLetterId));

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
                if (currentWord.equals(etWord.getText().toString().trim().toLowerCase())) {
                    activeTextViewLetter.setBackgroundColor(Color.GREEN);
                } else {
                    activeTextViewLetter.setBackgroundColor(Color.RED);
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
            int butLetter = buttonIds.getResourceId(i, 0);
            findViewById(butLetter).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loadWordAndDefinition((TextView) view);
                }
            });
        }

        if (!currentWord.equals("")) {
            etWord.setHint(currentWord);
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
        String startsWith = textView.getText().toString().toLowerCase();
        Word w = DBAccess.getRWordStarting(startsWith, res);
        if (w != null) {
            currentWord = w.getName().toLowerCase();
            tvDefinition.setText(w.getRandomDefinition());
            if (debug) {
                etWord.setHint(w.getName());
            }
        } else {
            // TODO: react to error
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

}
