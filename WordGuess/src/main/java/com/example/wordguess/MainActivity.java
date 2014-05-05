package com.example.wordguess;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class MainActivity extends Activity {

    TextView tvDefinition;
    EditText etWord;
    Button butSend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDefinition = (TextView) findViewById(R.id.tv_definition);
        etWord = (EditText) findViewById(R.id.et_word);
        // TODO: this should be an array in resources, but for some reason it doesn't want to work
        int[] ids = {R.id.but_a,
                R.id.but_b,
                R.id.but_c,
                R.id.but_d,
                R.id.but_e,
                R.id.but_f,
                R.id.but_g,
                R.id.but_h,
                R.id.but_i,
                R.id.but_j,
                R.id.but_k,
                R.id.but_l,
                R.id.but_m,
                R.id.but_n,
                R.id.but_nn,
                R.id.but_o,
                R.id.but_p,
                R.id.but_q,
                R.id.but_r,
                R.id.but_s,
                R.id.but_t,
                R.id.but_u,
                R.id.but_v,
                R.id.but_w,
                R.id.but_x,
                R.id.but_y,
                R.id.but_z};

        for (int i = 0; i < ids.length; i++) {

            int butLetter = ids[i];

            ((Button) findViewById(butLetter)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // when a letter is clicked, it retrieves the second word starting with that
                    // letter and its first definition
                    // it sets etWord's hint to the word (stupid debugging)
                    String text = "";
                    try {
                        String filename = ((Button) view).getText().toString().toLowerCase() + ".json";
                        InputStream inputStream = getResources().getAssets().open(filename);
                        text = fromInputStreamToString(inputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        JSONObject jsonObject = (JSONObject) new JSONTokener(text).nextValue();
                        JSONArray words = jsonObject.getJSONArray(getString(R.string.words_json_array_name));
                        JSONObject word = (JSONObject) words.get(2);
                        etWord.setHint(word.getString(getString(R.string.word_name_json)));
                        JSONArray definitions = word.getJSONArray(getString(R.string.definitions_json_array_name));
                        tvDefinition.setText(definitions.getString(0));

                    } catch (JSONException e) {
                        e.printStackTrace();
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

    /**
     * read a file and converting it to String using StringWriter
     */
    public static String fromInputStreamToString(InputStream inputStream) throws IOException {

        char[] buff = new char[1024];
        Writer stringWriter = new StringWriter();

        try {
            Reader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            int n;
            while ((n = bReader.read(buff)) != -1) {
                stringWriter.write(buff, 0, n);
            }
        } finally {
            stringWriter.close();
            inputStream.close();
        }
        return stringWriter.toString();
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
