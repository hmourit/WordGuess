package com.example.wordguess;

import android.content.res.Resources;
import android.util.Log;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DBAccess {

    private static Random random = new Random();

    public static Word getRWordStarting(String starts, Resources res) {
        String text = "";
        Word result_word = null;
        try {
            String filename;
            if (starts.equals("ñ")) {
                filename = "nn.json";
            } else {
                filename = starts + ".json";
            }
            InputStream inputStream = res.getAssets().open(filename);
            Log.d("BLA", inputStream.toString());
            text = fromInputStreamToString(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonObject = (JSONObject) new JSONTokener(text).nextValue();
            JSONArray words = jsonObject.getJSONArray(res.getString(R.string.words_json_array_name));
            int index = random.nextInt(words.length());
            JSONObject word = (JSONObject) words.get(index);

            String name = word.getString(res.getString(R.string.word_name_json));
            String starts_with = word.getString(res.getString(R.string.word_starts_json));

            JSONArray jDefinitions = word.getJSONArray(res.getString(R.string.definitions_json_array_name));
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < jDefinitions.length(); i++) {
                list.add(jDefinitions.getString(i));
            }

            int n_def = list.size();

            String[] definitions = list.toArray(new String[n_def]);

            result_word = new Word(name, definitions, n_def, starts_with);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result_word;
    }

    /**
     * read a file and converting it to String using StringWriter
     */
    private static String fromInputStreamToString(InputStream inputStream) throws IOException {

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
}
