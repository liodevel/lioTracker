package com.liodevel.lioapp_1.Activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.liodevel.lioapp_1.Utils.Server;
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
    private boolean favorites = false;

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tracks);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.liodevel_white)));
        changeNotificationBar();

        context = this;
        tracks = new ArrayList<>();
        tracksParseObject = new ArrayList<>();

        // Comprobar si es pantalla de favoritos
        if (getIntent().getStringExtra("favorites") != null && getIntent().getStringExtra("favorites").equals("1")){
            favorites = true;
            getSupportActionBar().setTitle(R.string.my_favorite_tracks);
        }

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
                                Server.deleteSelectedTracks(selectedTracksId, context);
                                adapter.clear();
                                selecting = false;
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
    private void getTracksByCurrentUser() {
        Utils.logInfo("getTracksByUser()");
        progress = new ProgressDialog(context);
        progress.setMessage(context.getResources().getString(R.string.loading_your_tracks));
        progress.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("track");
        ParseUser user = ParseUser.getCurrentUser();
        query.whereEqualTo("user", user);
        if (favorites){
            query.whereEqualTo("favorite", true);
        }
        query.orderByDescending("date");

        try {
            List<ParseObject> objects = query.find();
            for (ParseObject parseObject : objects) {
                Track track = new Track();
                track.setObjectId(parseObject.getObjectId());
                track.setDate((Date) parseObject.get("date"));
                track.setDateEnd((Date) parseObject.get("dateEnd"));
                track.setDistance((float) parseObject.getDouble("distance"));
                track.setInfo((String) parseObject.get("info"));
                track.setFavorite(parseObject.getBoolean("favorite"));
                track.setClosed(parseObject.getBoolean("closed"));
                Utils.logInfo("Track: " + track.getDate());
                if (!track.isClosed()) {
                    Utils.logInfo("Track Incomplete");
                    Server.fixTrack(track.getObjectId());
                }
                //tracks.add(track);
                adapter.add(track);
                tracksParseObject.add(parseObject);
            }
            //adapter.addAll(tracks);
            progress.dismiss();

        } catch (Exception e) {
            Utils.logInfo("Error: " + e.toString());

        }

    }







    @TargetApi(21)
    private void changeNotificationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Utils.logInfo("Notif.Bar.Coloured");
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(this.getResources().getColor(R.color.liodevel_dark_green));
        } else {
            Utils.logInfo("Ap");
        }
    }

}





