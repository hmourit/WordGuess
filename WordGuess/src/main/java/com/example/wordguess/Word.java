package com.example.wordguess;

import java.util.Random;

public class Word {
    private String name;
    private String[] definitions;
    private int n_definitions;
    private Random random;

    public Word(String name, String[] definitions, int n_definitions) {
        this.name = name;
        this.definitions = definitions;
        this.n_definitions = n_definitions;
        this.random = new Random();
    }

    public String getName() {
        return name;
    }

    public String[] getDefinitions() {
        return definitions;
    }

    public int getN_definitions() {
        return n_definitions;
    }

    public String getRandomDefinition(){
        String result;
        if(this.n_definitions == 1){
            result = this.definitions[0];
        }else{
            int index = random.nextInt(this.n_definitions);

            result = this.definitions[index];
        }

        return result;
    }
}
