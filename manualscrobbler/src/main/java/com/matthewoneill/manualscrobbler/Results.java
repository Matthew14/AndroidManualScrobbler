package com.matthewoneill.manualscrobbler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import de.umass.lastfm.*;
import de.umass.lastfm.scrobble.ScrobbleResult;

import java.util.ArrayList;
import java.util.Collection;

public class Results extends Activity {

    Session lfmSession = null;

    String key = "eb1f248f2a3dd23c0d321448d1b74c8a";
    String secret = "bfd72e05278afbd59b4b27e62036a115";
    String user;
    String password;

    String term;

    ArrayList<String> listItems = new ArrayList<>();
    ArrayList<Album> albumResults = new ArrayList<>();
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Caller.getInstance().setCache(null);
        Caller.getInstance().setUserAgent("tst");

        Intent i = getIntent();

        term = i.getStringExtra("searchTerm");
        user = i.getStringExtra("username");
        password = i.getStringExtra("password");

        this.setTitle(term);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);

        AlertDialog.Builder builder = new AlertDialog.Builder(Results.this);
        builder.setTitle("No Results");
        builder.setMessage("Search for " + term + " returned no results.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                finish();
            }
        });

        final AlertDialog dialog = builder.create();

        ListView lv= ((ListView)findViewById(R.id.gridview));
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                progress = ProgressDialog.show(Results.this, "Scrobbling " + albumResults.get(position).getName(), "Please Wait...", true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Album selectedAlbum = albumResults.get(position);
                        selectedAlbum = Album.getInfo(selectedAlbum.getArtist(), selectedAlbum.getName(), key);
                        Collection<Track> tracks = selectedAlbum.getTracks();

                        int totalDuration = 0;
                        for (Track t : tracks)
                            totalDuration += t.getDuration();

                        int startTime = (int) (System.currentTimeMillis() / 1000) - totalDuration;

                        boolean failed = false;

                        for (Track t : tracks) {
                            ScrobbleResult result = Track.scrobble(t.getArtist(), t.getName(), startTime, lfmSession);
                            boolean worked = result.isSuccessful() && !result.isIgnored();

                            startTime += t.getDuration();

                            if (!worked) {
                                failed = true;
                                break;
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                            }
                        });

                        ToastIt(failed ? "One or more tracks failed to scrobble" : selectedAlbum.getName() + " scrobbled!");
                    }
                }).start();
            }
        });

        progress = ProgressDialog.show(Results.this,"Searching for "+term, "Please Wait...", true);

        new Thread(new Runnable() {


            @Override
            public void run() {

                lfmSession = Authenticator.getMobileSession(user, password, key, secret);

                if(lfmSession == null)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.dismiss();
                            ToastIt("Incorrect credentials");
                            finish();
                        }
                    });
                }

                Collection<Album> res = Album.search(term, key);
                albumResults.clear();
                for (final Album a: res){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String s = String.format("%s - %s", a.getArtist(), a.getName());
                            listItems.add(s);
                            albumResults.add(a);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
                runOnUiThread(new Runnable() {@Override public void run(){progress.dismiss();}});

                if(res.size()<1)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.show();
                        }
                    });
                }

            }
        }).start();

    }

    private void ToastIt(final String s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Results.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
