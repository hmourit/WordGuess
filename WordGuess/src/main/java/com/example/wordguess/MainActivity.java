package com.example.wordguess;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends Activity {

    Button new_game;
    Button cont_game;

    private boolean checkSaveFile(){
        boolean result = false;
        String filename = getResources().getString(R.string.save_file) + ".json";
        ArrayList<String> files = new ArrayList<>(Arrays.asList(fileList()));
        if(files.contains(filename)){
            result = true;
        }
        return result;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new_game = (Button) findViewById(R.id.but_start);
        cont_game = (Button) findViewById(R.id.but_cont);


        if(checkSaveFile()){
            cont_game.setEnabled(true);
        }else{
            cont_game.setEnabled(false);
        }
        setCallbacks();

        /*if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkSaveFile()){
            cont_game.setEnabled(true);
        }else{
            cont_game.setEnabled(false);
        }
    }

    private void setCallbacks(){
        new_game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), GameActivity.class);
                i.setAction(getResources().getString(R.string.new_game_intent_action));
                startActivity(i);
            }
        });

        cont_game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), GameActivity.class);
                i.setAction(getResources().getString(R.string.continue_game_intent_action));
                startActivity(i);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_main, container, false);
            return rootView;
        }
    }

}
