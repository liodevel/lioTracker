package com.liodevel.lioapp_1.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.liodevel.lioapp_1.Objects.Track;
import com.liodevel.lioapp_1.Objects.TrackPoint;
import com.liodevel.lioapp_1.R;
import com.liodevel.lioapp_1.Adapters.MyTracksListAdapter;
import com.liodevel.lioapp_1.Utils.Utils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Pantalla My tracks
 * Con la lista de tracks guardadas
 */
public class MyTracksActivity extends AppCompatActivity {

    private static ArrayList<Track> tracks;
    private static ArrayList<ParseObject> tracksParseObject;
    private Menu actionBarMenu;
    private ListView tracksList;
    private static MyTracksListAdapter adapter;
    private static Context context;
    private int selectedPosition = -1;
    private ArrayList<String> selectedTracksId = new ArrayList<>();
    private static ProgressDialog progress;
    private boolean selecting = false;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tracks);

        context = this;
        tracks = new ArrayList<>();
        tracksParseObject = new ArrayList<>();

        // Lista de tracks
        tracksList = (ListView) findViewById(R.id.tracks_list);

        tracksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!selecting) {

                    Utils.logInfo("Track selected: " + tracks.get(position).getObjectId());
                    progress = new ProgressDialog(context);
                    progress.setMessage(getResources().getString(R.string.loading_track));
                    progress.show();

                    Intent launchNextActivity;
                    launchNextActivity = new Intent(MyTracksActivity.this, TrackActivity.class);
                    launchNextActivity.putExtra("objectId", tracks.get(position).getObjectId());

                    startActivity(launchNextActivity);
                } else {
                    if (!selectedTracksId.contains(tracks.get(position).getObjectId())) {
                        Utils.logInfo("SELECT");
                        //view.setBackground(getResources().getDrawable(R.drawable.item_selected));
                        tracks.get(position).setIsChecked(true);
                        actionBarMenu.findItem(R.id.map_action_delete_my_tracks).setVisible(true);
                        selecting = true;
                        selectedTracksId.add(tracks.get(position).getObjectId());
                        Utils.logInfo("SELECTED tracks: " + selectedTracksId.toString());
                    } else {
                        Utils.logInfo("DESELECT");
                        //view.setBackground(getResources().getDrawable(R.drawable.item));
                        tracks.get(position).setIsChecked(false);
                        selectedTracksId.remove(tracks.get(position).getObjectId());
                        Utils.logInfo("SELECTED tracks: " + selectedTracksId.toString());
                        // Si no hay nada seleccionado esconder Trash
                        if (selectedTracksId.size() == 0) {
                            actionBarMenu.findItem(R.id.map_action_delete_my_tracks).setVisible(false);
                            selecting = false;
                        }
                    }
                    Utils.logInfo("LONG CLICK: ObjectID: " + tracks.get(position).getObjectId());
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
                    Utils.logInfo("SELECT");
                    //view.setBackground(getResources().getDrawable(R.drawable.item_selected));
                    tracks.get(position).setIsChecked(true);
                    actionBarMenu.findItem(R.id.map_action_delete_my_tracks).setVisible(true);
                    selecting = true;
                    selectedTracksId.add(tracks.get(position).getObjectId());
                    Utils.logInfo("SELECTED tracks: " + selectedTracksId.toString());
                } else {
                    Utils.logInfo("DESELECT");
                    //view.setBackground(getResources().getDrawable(R.drawable.item));
                    tracks.get(position).setIsChecked(false);
                    selectedTracksId.remove(tracks.get(position).getObjectId());
                    Utils.logInfo("SELECTED tracks: " + selectedTracksId.toString());
                    // Si no hay nada seleccionado esconder Trash
                    if (selectedTracksId.size() == 0) {
                        actionBarMenu.findItem(R.id.map_action_delete_my_tracks).setVisible(false);
                        selecting = false;
                    }
                }
                Utils.logInfo("LONG CLICK: ObjectID: " + tracks.get(position).getObjectId());
                adapter.notifyDataSetChanged();
                return true;
            }
        });

        adapter = new MyTracksListAdapter(this, tracks);
        tracksList.setAdapter(adapter);
        adapter.clear();
        getTracksByCurrentUser();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        adapter.clear();
        try {
            progress.dismiss();
        } catch (Exception e) {

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
                        .setMessage(getResources().getString(R.string.delete_selected_tracks))
                        .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                actionBarMenu.findItem(R.id.map_action_delete_my_tracks).setVisible(false);
                                deleteSelectedTracks();
                                adapter.clear();
                                getTracksByCurrentUser();
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
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
    private static void getTracksByCurrentUser() {
        Utils.logInfo("getTracksByUser()");
        progress = new ProgressDialog(context);
        progress.setMessage(context.getResources().getString(R.string.loading_your_tracks));
        progress.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("track");
        ParseUser user = ParseUser.getCurrentUser();
        query.whereEqualTo("user", user);
        query.orderByDescending("date");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject parseObject : objects) {
                        Track track = new Track();
                        track.setObjectId(parseObject.getObjectId());
                        track.setDate((Date) parseObject.get("date"));
                        track.setDateEnd((Date) parseObject.get("dateEnd"));
                        track.setDistance((float) parseObject.getDouble("distance"));
                        track.setInfo((String) parseObject.get("info"));
                        track.setFavorite(parseObject.getBoolean("favorite"));
                        Utils.logInfo("Track: " + track.getDate());
                        if (track.getDateEnd() == null){
                            Utils.logInfo("Track Incomplete");
                            fixTrack(track.getObjectId());
                        }
                        //tracks.add(track);
                        adapter.add(track);
                        tracksParseObject.add(parseObject);
                    }
                    //adapter.addAll(tracks);
                    progress.dismiss();
                } else {
                    // Something went wrong.
                    Utils.logInfo("Error: " + e.toString());
                }
            }
        });
    }


    /**
     * Borra un Track
     *
     * @param objectId
     */
    public void deleteTrackByObjectId(String objectId) {
        Utils.logInfo("deleteTrackByObjectId()");
        ParseObject trackObject = null;
        ParseQuery<ParseObject> queryTrackObject = ParseQuery.getQuery("track");
        queryTrackObject.whereEqualTo("objectId", objectId);
        try {
            List<ParseObject> parseQueriesTrackObject = queryTrackObject.find();
            trackObject = parseQueriesTrackObject.get(0);
            trackObject.delete();
            //trackObject.save();
            Utils.logInfo("Track ID: " + trackObject.getObjectId());

        } catch (ParseException e) {
            Utils.logInfo("Error deleting: " + e.toString());
        }
    }


    /**
     * Borra los Tracks seleccionados
     */
    private void deleteSelectedTracks() {
        Utils.logInfo("deleteSelectedTracks()");
        if (selectedTracksId.size() > 0) {
            ParseObject trackObject = null;
            ParseQuery<ParseObject> queryTrackObject = ParseQuery.getQuery("track");
            try {
                int cont = 0;
                for (String id : selectedTracksId) {
                    queryTrackObject.whereEqualTo("objectId", id);
                    List<ParseObject> parseQueriesTrackObject = queryTrackObject.find();
                    Utils.logInfo("Find: " + parseQueriesTrackObject.toString());
                    trackObject = parseQueriesTrackObject.get(0);
                    trackObject.delete();
                    //trackObject.save();
                    Utils.logInfo("Track ID: " + trackObject.getObjectId());
                    cont++;
                }
                selectedTracksId = new ArrayList<>();
                Utils.showMessage(getApplicationContext(), cont + " " + getResources().getString(R.string.tracks_deleted));
            } catch (ParseException e) {
                Utils.logInfo("Error deleting: " + e.toString());
                Utils.showMessage(getApplicationContext(), getResources().getString(R.string.error_deleting_tracks));
            }
        }
    }

    /**
     *
     * @param objectId
     */
    private static void fixTrack(String objectId){

        ParseObject trackObject = null;
        Track currentTrack = new Track();

        boolean ret = false;
        LatLng prevPos = null;
        LatLng actualPos = null;
        TrackPoint previousTrackPoint = new TrackPoint();

        ParseQuery<ParseObject> queryTrackObject = ParseQuery.getQuery("track");
        queryTrackObject.whereEqualTo("objectId", objectId);

        try {
            List<ParseObject> parseQueriesTrackObject = queryTrackObject.find();
            trackObject = parseQueriesTrackObject.get(0);
            currentTrack.setObjectId(parseQueriesTrackObject.get(0).getObjectId());
            currentTrack.setDate((Date) parseQueriesTrackObject.get(0).get("date"));
            currentTrack.setDateEnd((Date) parseQueriesTrackObject.get(0).get("dateEnd"));
            currentTrack.setDistance((float) parseQueriesTrackObject.get(0).getDouble("distance"));
            currentTrack.setInfo((String) parseQueriesTrackObject.get(0).get("info"));
            currentTrack.setFavorite(parseQueriesTrackObject.get(0).getBoolean("favorite"));
            Utils.logInfo("Track ID: " + trackObject.getObjectId());
            ret = true;
        } catch (ParseException e) {
            Utils.logInfo("Error: " + e.toString());
            ret = false;
        }

        if (trackObject != null) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("trackPoint");
            query.whereEqualTo("track", trackObject);
            query.setLimit(1000);
            query.orderByAscending("date");
            int cont = 0;
            try {
                List<ParseObject> parseQueries = query.find();
                float totalDistance = 0;
                Date lastDate = null;
                for (ParseObject parseObject : parseQueries) {
                    cont++;
                    TrackPoint trackPoint = new TrackPoint();
                    trackPoint.setObjectId(parseObject.getObjectId());
                    trackPoint.setDate((Date) parseObject.get("date"));
                    lastDate = (Date) parseObject.get("date");
                    trackPoint.setPosition((ParseGeoPoint) parseObject.get("position"));
                    actualPos = new LatLng(trackPoint.getPosition().getLatitude(), trackPoint.getPosition().getLongitude());
                    if (prevPos != null) {
                        if(previousTrackPoint != null) {

                            Location selected_location=new Location("locationA");
                            selected_location.setLatitude(trackPoint.getPosition().getLatitude());
                            selected_location.setLongitude( trackPoint.getPosition().getLongitude());
                            Location near_locations=new Location("locationA");
                            near_locations.setLatitude(previousTrackPoint.getPosition().getLatitude());
                            near_locations.setLongitude(previousTrackPoint.getPosition().getLongitude());

                            double distance = selected_location.distanceTo(near_locations);
                            totalDistance = totalDistance + ((float)distance);
                        }
                    }

                    prevPos = actualPos;
                    previousTrackPoint = trackPoint;
                }
                currentTrack.setDistance(totalDistance);
                trackObject.put("distance", totalDistance);
                trackObject.put("dateEnd", lastDate);
                trackObject.save();
                Utils.logInfo("Track Fixed! ");
                ret = true;
            } catch (ParseException e) {
                Utils.logInfo("Error: " + e.toString());
                ret = false;
            }
        }


    }

}





