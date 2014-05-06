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


public class MainActivity extends Activity {

    TextView tvDefinition;
    EditText etWord;
    Button butSend;

    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDefinition = (TextView) findViewById(R.id.tv_definition);
        etWord = (EditText) findViewById(R.id.et_word);

        res = getResources();
        TypedArray tarray = res.obtainTypedArray(R.array.button_ids);

        for(int i = 0; i< tarray.length(); i++){
            int butLetter = tarray.getResourceId(i,0);

            ((Button) findViewById(butLetter)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // when a letter is clicked, it retrieves a random word starting with that
                    // letter and a random definition
                    // it sets etWord's hint to the word (stupid debugging)
                    String starts = ((Button) view).getText().toString().toLowerCase();
                    Word w = DBAccess.getRWordStarting(starts, res);
                    if (w == null){
                        etWord.setHint("null");
                    }else{
                        etWord.setHint(w.getName());
                        tvDefinition.setText(w.getRandomDefinition());
                    }
                }
            });
        }

        butSend = (Button) findViewById(R.id.but_send);
        butSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // checks whether hint and written text are equal
                if (etWord.getHint().equals(etWord.getText().toString().trim())) {
                    etWord.setBackgroundColor(Color.GREEN);
                } else {
                    etWord.setBackgroundColor(Color.RED);
                }
            }
        });
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
