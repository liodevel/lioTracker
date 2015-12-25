package com.liodevel.lioapp_1.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.liodevel.lioapp_1.Objects.Track;
import com.liodevel.lioapp_1.Objects.TrackPoint;
import com.liodevel.lioapp_1.R;
import com.liodevel.lioapp_1.Utils.Server;
import com.liodevel.lioapp_1.adapters.MyTracksListAdapter;
import com.parse.FindCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Pantalla My tracks
 * Con la lista de tracks guardadas
 */
public class MyTracksActivity extends AppCompatActivity {

    static ArrayList<Track> tracks;
    Menu actionBarMenu;
    ListView tracksList;
    static MyTracksListAdapter adapter;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tracks);
        context = this;
        tracks = new ArrayList<>();

        // Toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_tracks_toolbar);
        setSupportActionBar(myToolbar);

        // Lista de tracks
        tracksList = (ListView) findViewById(R.id.tracks_list);
        tracksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("LIOTRACKS", "Track selected: " + tracks.get(position).getObjectId());
                Intent launchNextActivity;
                launchNextActivity = new Intent(MyTracksActivity.this, TrackActivity.class);
                launchNextActivity.putExtra("objectId", tracks.get(position).getObjectId());

                startActivity(launchNextActivity);

            }
        });
        adapter = new MyTracksListAdapter(this, tracks);
        tracksList.setAdapter(adapter);
        getTracksByCurrentUser();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        adapter.clear();
        getTracksByCurrentUser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionBarMenu = menu;
        getMenuInflater().inflate(R.menu.menu_actionbar_my_tracks, menu);
        return true;
    }


    public static void getTracksByCurrentUser(){
        Log.i("LIOTRACK", "getTracksByUser()");

        ParseQuery<ParseObject> query = ParseQuery.getQuery("track");
        ParseUser user = ParseUser.getCurrentUser();
        query.whereEqualTo("user", user);
        query.orderByDescending("date");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, com.parse.ParseException e) {
                if (e == null) {
                    for (ParseObject parseObject : objects) {
                        Track track = new Track();
                        track.setObjectId(parseObject.getObjectId());
                        track.setDate((Date) parseObject.get("date"));
                        track.setDateEnd((Date) parseObject.get("dateEnd"));
                        Log.i("LIOTRACK", "Track: " + track.getDate());
                        adapter.add(track);
                    }
                } else {
                    // Something went wrong.
                    Log.i("LIOTRACK", "Error: " + e.toString());
                }
            }
        });
    }



}
