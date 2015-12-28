package com.liodevel.lioapp_1.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.liodevel.lioapp_1.Objects.Track;
import com.liodevel.lioapp_1.R;
import com.liodevel.lioapp_1.Adapters.MyTracksListAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
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
    String selectedTrackObjectId = "";
    int selectedPosition = -1;

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
        tracksList.setLongClickable(true);
        tracksList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        tracksList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                for (int cont = 0; cont < tracksList.getCount(); cont++){
                    Log.i("LIOTRACK", "LISTCOUNT: " + cont);
                    tracksList.getChildAt(cont).setBackground(getResources().getDrawable(R.drawable.item));
                }

                if (!tracksList.isItemChecked(position)){
                    Log.i("LIOTRACK", "NO SELECTED");
                    tracksList.setSelection(position);
                    tracksList.setItemChecked(position, true);
                    view.setBackground(getResources().getDrawable(R.drawable.item_selected));
                    actionBarMenu.findItem(R.id.map_action_delete_my_tracks).setVisible(true);
                    selectedTrackObjectId = tracks.get(position).getObjectId();
                    selectedPosition = position;
                } else {
                    Log.i("LIOTRACK", "SELECTED");
                    tracksList.setSelection(position);
                    tracksList.setItemChecked(position, false);
                    view.setBackground(getResources().getDrawable(R.drawable.item));
                    actionBarMenu.findItem(R.id.map_action_delete_my_tracks).setVisible(false);
                    selectedTrackObjectId = "";
                    selectedPosition = -1;
                }
                Log.i("LIOTRACK", "LONG CLICK: ObjectID: " + tracks.get(position).getObjectId());
                return true;
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_action_delete_my_tracks:
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder
                        .setMessage("Delete selected track")
                        .setPositiveButton("Yes",  new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                actionBarMenu.findItem(R.id.map_action_delete_my_tracks).setVisible(false);
                                deleteTrackByObjectId(selectedTrackObjectId);
                                adapter.clear();
                                getTracksByCurrentUser();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        })
                        .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void deleteTrackByObjectId(String objectId){
        Log.i("LIOTRACK", "deleteTrackByObjectId()");
        ParseObject trackObject = null;
        ParseQuery<ParseObject> queryTrackObject = ParseQuery.getQuery("track");
        queryTrackObject.whereEqualTo("objectId", objectId);
        try {
            List <ParseObject> parseQueriesTrackObject = queryTrackObject.find();
            trackObject = parseQueriesTrackObject.get(0);
            trackObject.delete();
            trackObject.save();
            Log.i("LIOTRACK", "Track ID: " + trackObject.getObjectId());

        } catch (ParseException e) {
            Log.i("LIOTRACK", "Error deleting: " + e.toString());
        }
    }



}
