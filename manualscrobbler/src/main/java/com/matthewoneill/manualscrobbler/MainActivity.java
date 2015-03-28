package com.matthewoneill.manualscrobbler;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity {

    EditText searchStringET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchStringET = (EditText) findViewById(R.id.searchEditText);
        findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearch(searchStringET.getText().toString().trim().toLowerCase());
            }
        });
    }

    private void ToastIt(final String s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void doSearch(String text) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        if(!preferences.contains("lastfm_username") || !preferences.contains("lastfm_password"))
        {
            ToastIt("Please login in the settings.");
            return;
        }

        Intent myIntent = new Intent(MainActivity.this, Results.class);

        myIntent.putExtra("searchTerm", text);
        myIntent.putExtra("username", preferences.getString("lastfm_username", ""));
        myIntent.putExtra("password", preferences.getString("lastfm_password", ""));

        MainActivity.this.startActivity(myIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
