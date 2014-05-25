package com.example.wordguess;

import android.content.res.Resources;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

public class Word implements Serializable {
    private String name;
    private String startsWith;
    private String[] definitions;
    private int nDefinitions;
    private Random random;
    private String activeDef;

    public Word(String name, String[] definitions, int nDefinitions, String startsWith) {
        this.name = name;
        this.definitions = definitions;
        this.nDefinitions = nDefinitions;
        this.random = new Random();
        this.startsWith = startsWith;
    }

    public Word(JSONObject obj, Resources res){
        try {
            this.name = obj.getString(res.getString(R.string.save_word_name_json));
            this.activeDef = obj.getString(res.getString(R.string.save_act_definition_json));
            this.startsWith = obj.getString(res.getString(R.string.save_starts_word_json));
            this.random = new Random();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public String getStartsWith() {
        return startsWith;
    }

    public String getActiveDef() {
        return activeDef;
    }

    public void setActiveDef() {
        String result;
        if (this.nDefinitions == 1) {
            result = this.definitions[0];
        } else {
            int index = random.nextInt(this.nDefinitions);

            result = this.definitions[index];
        }

        this.activeDef = result;
    }

    public JSONObject toJsonObject(Resources res){
        JSONObject obj = new JSONObject();
        try {
            obj.put(res.getString(R.string.save_word_name_json), this.name);
            obj.put(res.getString(R.string.save_act_definition_json), this.activeDef);
            obj.put(res.getString(R.string.save_starts_word_json), this.startsWith);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Word)) return false;

        Word word = (Word) o;

        if (nDefinitions != word.nDefinitions) return false;
        if (!activeDef.equals(word.activeDef)) return false;
        if (!Arrays.equals(definitions, word.definitions)) return false;
        if (!name.equals(word.name)) return false;
        if (!startsWith.equals(word.startsWith)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + startsWith.hashCode();
        result = 31 * result + Arrays.hashCode(definitions);
        result = 31 * result + nDefinitions;
        result = 31 * result + activeDef.hashCode();
        return result;
    }


}
