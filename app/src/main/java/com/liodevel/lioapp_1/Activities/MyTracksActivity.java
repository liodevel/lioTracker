package com.liodevel.lioapp_1.Activities;

import android.app.ProgressDialog;
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
import com.liodevel.lioapp_1.Utils.Utils;
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

    private static ArrayList<Track> tracks;
    private Menu actionBarMenu;
    private ListView tracksList;
    private static MyTracksListAdapter adapter;
    private static Context context;
    private int selectedPosition = -1;
    private ArrayList<String> selectedTracksId = new ArrayList<String>();
    private static ProgressDialog progress;
    private boolean selecting = false;

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

                if (!selecting) {

                    Log.i("LIOTRACKS", "Track selected: " + tracks.get(position).getObjectId());
                    progress = new ProgressDialog(context);
                    progress.setMessage("Loading track");
                    progress.show();

                    Intent launchNextActivity;
                    launchNextActivity = new Intent(MyTracksActivity.this, TrackActivity.class);
                    launchNextActivity.putExtra("objectId", tracks.get(position).getObjectId());

                    startActivity(launchNextActivity);
                } else {
                    if (!selectedTracksId.contains(tracks.get(position).getObjectId())) {
                        Log.i("LIOTRACK", "SELECT");
                        //view.setBackground(getResources().getDrawable(R.drawable.item_selected));
                        tracks.get(position).setIsChecked(true);
                        actionBarMenu.findItem(R.id.map_action_delete_my_tracks).setVisible(true);
                        selecting = true;
                        selectedTracksId.add(tracks.get(position).getObjectId());
                        Log.i("LIOTRACK", "SELECTED tracks: " + selectedTracksId.toString());
                    } else {
                        Log.i("LIOTRACK", "DESELECT");
                        //view.setBackground(getResources().getDrawable(R.drawable.item));
                        tracks.get(position).setIsChecked(false);
                        selectedTracksId.remove(tracks.get(position).getObjectId());
                        Log.i("LIOTRACK", "SELECTED tracks: " + selectedTracksId.toString());
                        // Si no hay nada seleccionado esconder Trash
                        if (selectedTracksId.size() == 0) {
                            actionBarMenu.findItem(R.id.map_action_delete_my_tracks).setVisible(false);
                            selecting = false;
                        }
                    }
                    Log.i("LIOTRACK", "LONG CLICK: ObjectID: " + tracks.get(position).getObjectId());
                    adapter.notifyDataSetChanged();

                }
            }
        });
        tracksList.setLongClickable(true);
        tracksList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        tracksList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (!selectedTracksId.contains(tracks.get(position).getObjectId())) {
                    Log.i("LIOTRACK", "SELECT");
                    //view.setBackground(getResources().getDrawable(R.drawable.item_selected));
                    tracks.get(position).setIsChecked(true);
                    actionBarMenu.findItem(R.id.map_action_delete_my_tracks).setVisible(true);
                    selecting = true;
                    selectedTracksId.add(tracks.get(position).getObjectId());
                    Log.i("LIOTRACK", "SELECTED tracks: " + selectedTracksId.toString());
                } else {
                    Log.i("LIOTRACK", "DESELECT");
                    //view.setBackground(getResources().getDrawable(R.drawable.item));
                    tracks.get(position).setIsChecked(false);
                    selectedTracksId.remove(tracks.get(position).getObjectId());
                    Log.i("LIOTRACK", "SELECTED tracks: " + selectedTracksId.toString());
                    // Si no hay nada seleccionado esconder Trash
                    if (selectedTracksId.size() == 0) {
                        actionBarMenu.findItem(R.id.map_action_delete_my_tracks).setVisible(false);
                        selecting = false;
                    }
                }
                Log.i("LIOTRACK", "LONG CLICK: ObjectID: " + tracks.get(position).getObjectId());
                adapter.notifyDataSetChanged();
                return true;
            }
        });

        adapter = new MyTracksListAdapter(this, tracks);
        tracksList.setAdapter(adapter);
        adapter.clear();
        getTracksByCurrentUser();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        adapter.clear();
        try {
            progress.dismiss();
        } catch (Exception e){

        }
        getTracksByCurrentUser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionBarMenu = menu;
        getMenuInflater().inflate(R.menu.menu_actionbar_my_tracks, menu);
        return true;
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
                                deleteSelectedTracks();
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



    /**
     * Recupera la lista de Tracks
     */
    public static void getTracksByCurrentUser(){
        Log.i("LIOTRACK", "getTracksByUser()");
        progress = new ProgressDialog(context);
        progress.setMessage("Loading your tracks");
        progress.show();

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
                        track.setDistance((float) parseObject.getDouble("distance"));
                        Log.i("LIOTRACK", "Track: " + track.getDate());
                        //tracks.add(track);
                        adapter.add(track);
                    }
                    //adapter.addAll(tracks);
                    progress.dismiss();
                } else {
                    // Something went wrong.
                    Log.i("LIOTRACK", "Error: " + e.toString());
                }
            }
        });
    }


    /**
     * Borra un Track
     * @param objectId
     */
    public void deleteTrackByObjectId(String objectId){
        Log.i("LIOTRACK", "deleteTrackByObjectId()");
        ParseObject trackObject = null;
        ParseQuery<ParseObject> queryTrackObject = ParseQuery.getQuery("track");
        queryTrackObject.whereEqualTo("objectId", objectId);
        try {
            List <ParseObject> parseQueriesTrackObject = queryTrackObject.find();
            trackObject = parseQueriesTrackObject.get(0);
            trackObject.delete();
            //trackObject.save();
            Log.i("LIOTRACK", "Track ID: " + trackObject.getObjectId());

        } catch (ParseException e) {
            Log.i("LIOTRACK", "Error deleting: " + e.toString());
        }
    }


    /**
     * Borra un Track
     */
    public void deleteSelectedTracks(){
        Log.i("LIOTRACK", "deleteSelectedTracks()");
        if (selectedTracksId.size() > 0) {
            ParseObject trackObject = null;
            ParseQuery<ParseObject> queryTrackObject = ParseQuery.getQuery("track");
            try {
                int cont = 0;
                for (String id : selectedTracksId) {
                    queryTrackObject.whereEqualTo("objectId", id);
                    List<ParseObject> parseQueriesTrackObject = queryTrackObject.find();
                    Log.i("LIOTRACK", "Find: " + parseQueriesTrackObject.toString());
                    trackObject = parseQueriesTrackObject.get(0);
                    trackObject.delete();
                    //trackObject.save();
                    Log.i("LIOTRACK", "Track ID: " + trackObject.getObjectId());
                    cont++;
                }
                Utils.showMessage(getApplicationContext(), cont + " tracks deleted");
            } catch (ParseException e) {
                Log.i("LIOTRACK", "Error deleting: " + e.toString());
                Utils.showMessage(getApplicationContext(), "Error deleting tracks :(");
            }
        }
    }


}
