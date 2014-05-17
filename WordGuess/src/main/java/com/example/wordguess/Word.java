package com.example.wordguess;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

public class Word{
    private String name;
    private String starts_with;
    private String[] definitions;
    private int n_definitions;
    private Random random;
    private String active_def;

    public Word(String name, String[] definitions, int n_definitions, String starts_with) {
        this.name = name;
        this.definitions = definitions;
        this.n_definitions = n_definitions;
        this.random = new Random();
        this.starts_with = starts_with;
    }

    public String getName() {
        return name;
    }

    public String getStarts_with(){
        return starts_with;
    }

    public String getActive_def(){
        return active_def;
    }

    public void setActive_def(){
        String result;
        if(this.n_definitions == 1){
            result = this.definitions[0];
        }else{
            int index = random.nextInt(this.n_definitions);

            result = this.definitions[index];
        }

        this.active_def = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Word)) return false;

        Word word = (Word) o;

        if (n_definitions != word.n_definitions) return false;
        if (!active_def.equals(word.active_def)) return false;
        if (!Arrays.equals(definitions, word.definitions)) return false;
        if (!name.equals(word.name)) return false;
        if (!starts_with.equals(word.starts_with)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + starts_with.hashCode();
        result = 31 * result + Arrays.hashCode(definitions);
        result = 31 * result + n_definitions;
        result = 31 * result + active_def.hashCode();
        return result;
    }


}
