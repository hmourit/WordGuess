package com.example.wordguess;

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
