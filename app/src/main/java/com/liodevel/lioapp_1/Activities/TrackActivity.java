package com.liodevel.lioapp_1.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.liodevel.lioapp_1.Objects.Track;
import com.liodevel.lioapp_1.Objects.TrackPoint;
import com.liodevel.lioapp_1.R;
import com.liodevel.lioapp_1.Utils.Utils;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TrackActivity extends AppCompatActivity {

    private GoogleMap mMap;
    private Menu actionBarMenu;
    private static Context context;
    private String trackObjectId = "";
    private Track currentTrack = new Track();
    private static ProgressDialog progress;
    ParseObject trackObject = null;

    private Toolbar myToolbar;
    private TextView durationInfo;
    private TextView distanceInfo;
    private TextView info;
    private EditText editInfo;

    static ArrayList<TrackPoint> trackPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = this;

        myToolbar = (Toolbar) findViewById(R.id.track_toolbar);
        setSupportActionBar(myToolbar);
        durationInfo = (TextView) findViewById(R.id.text_track_duration_track_info);
        distanceInfo = (TextView) findViewById(R.id.text_track_distance_track_info);
        info = (TextView) findViewById(R.id.text_track_info);
        editInfo = (EditText) findViewById(R.id.edit_info);

        //progressDialog.show(this, "Track", "Downloading track", true);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            trackObjectId = "";
        } else {
            trackObjectId = extras.getString("objectId");
            Log.i("LIOTRACKS", "ObjectId Track: " + trackObjectId);
        }

        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapTrack)).getMap();
            if (mMap == null) {
                Utils.showMessage(getApplicationContext(), "Sorry! unable to create maps");
            }
        }

        getTrackByObjectId(trackObjectId);
        updateTrackInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateTrack();
    }

    /**
     * Dibuja una linea en el mapa
     *
     * @param start
     * @param end
     */
    private void drawTrackPoint(LatLng start, LatLng end) {
        if (mMap != null) {
            PolylineOptions line =
                    new PolylineOptions().add(start, end)
                            .width(10).color(Color.BLACK);
            mMap.addPolyline(line);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionBarMenu = menu;
        getMenuInflater().inflate(R.menu.menu_actionbar_track, menu);

        // Favorito
        if (currentTrack != null && currentTrack.isFavorite()){
            actionBarMenu.findItem(R.id.track_action_favorite).setIcon(R.drawable.ic_favorite_black_36dp);;
        } else {
            actionBarMenu.findItem(R.id.track_action_favorite).setIcon(R.drawable.ic_favorite_border_black_36dp);;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_action_delete_track:
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder
                        .setMessage("Delete track")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                deleteTrackByObjectId(trackObjectId);
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .show();
                return true;

            // TIPO MAPA
            case R.id.track_action_type_map:
                toggleMapType();
                return true;

            // FAVORITO
            case R.id.track_action_favorite:
                updateTrackFavorite();
                return true;


            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Get currentTrack
     *
     * @param objectId
     * @return
     */
    private boolean getTrackByObjectId(String objectId) {
        Log.i("LIOTRACK", "getTrackByObjectId()");

        progress = new ProgressDialog(context);
        progress.setMessage("Loading track");
        progress.show();

        boolean ret = false;
        LatLng prevPos = null;
        LatLng actualPos = null;

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
            Log.i("LIOTRACK", "Track ID: " + trackObject.getObjectId());
            ret = true;
        } catch (ParseException e) {
            Log.i("LIOTRACK", "Error: " + e.toString());
            ret = false;
        }

        if (ret == true) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("trackPoint");
            query.whereEqualTo("track", trackObject);
            query.setLimit(1000);
            int cont = 0;
            try {
                List<ParseObject> parseQueries = query.find();
                for (ParseObject parseObject : parseQueries) {
                    cont++;
                    TrackPoint trackPoint = new TrackPoint();
                    trackPoint.setObjectId(parseObject.getObjectId());
                    trackPoint.setDate((Date) parseObject.get("date"));
                    trackPoint.setPosition((ParseGeoPoint) parseObject.get("position"));
                    //trackPoints.add(trackPoint);
                    actualPos = new LatLng(trackPoint.getPosition().getLatitude(), trackPoint.getPosition().getLongitude());
                    if (prevPos != null) {
                        drawTrackPoint(prevPos, actualPos);
                    } else {
                        // Centrar en primera localización
                        if (mMap != null){
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(actualPos, 16));
                        }
                    }
                    prevPos = actualPos;
                }
                Log.i("LIOTRACK", "TOTAL TrackPoints: " + cont);
                ret = true;
            } catch (ParseException e) {
                Log.i("LIOTRACK", "Error: " + e.toString());
                ret = false;
            }
        }
        progress.dismiss();
        return ret;
    }

    /**
     * Borrar Track
     * @param objectId
     */
    private void deleteTrackByObjectId(String objectId) {
        Log.i("LIOTRACK", "deleteTrackByObjectId()");
        ParseObject trackObject = null;

        ParseQuery<ParseObject> queryTrackObject = ParseQuery.getQuery("track");
        queryTrackObject.whereEqualTo("objectId", objectId);
        try {
            List<ParseObject> parseQueriesTrackObject = queryTrackObject.find();
            trackObject = parseQueriesTrackObject.get(0);
            trackObject.delete();
            trackObject.saveInBackground();
            Log.i("LIOTRACK", "Track ID: " + trackObject.getObjectId());

        } catch (ParseException e) {
            Log.i("LIOTRACK", "Error deleting: " + e.toString());
        }
    }

    private void updateTrackInfo() {
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentTrack.getDate());

        // Info
        if (currentTrack.getInfo() != null && currentTrack.getInfo().length() > 0) {
            info.setText(currentTrack.getInfo());
            editInfo.setText(currentTrack.getInfo());
        } else {
            info.setText("Insert track info here");
            editInfo.setText("Insert track info here");
            info.setTextColor(getResources().getColor(R.color.liodevel_grey));
        }

        // Date
        if (currentDate.getTime() - currentTrack.getDate().getTime() < TimeUnit.MILLISECONDS.convert(6, TimeUnit.DAYS)) {
            String weekDay = "";
            if (c.get(Calendar.DAY_OF_WEEK) == 1) {
                weekDay = Utils.SATURDAY;
            } else if (c.get(Calendar.DAY_OF_WEEK) == 2) {
                weekDay = Utils.MONDAY;
            } else if (c.get(Calendar.DAY_OF_WEEK) == 3) {
                weekDay = Utils.TUESDAY;
            } else if (c.get(Calendar.DAY_OF_WEEK) == 4) {
                weekDay = Utils.WEDNESDAY;
            } else if (c.get(Calendar.DAY_OF_WEEK) == 5) {
                weekDay = Utils.THURSDAY;
            } else if (c.get(Calendar.DAY_OF_WEEK) == 6) {
                weekDay = Utils.FRIDAY;
            } else if (c.get(Calendar.DAY_OF_WEEK) == 7) {
                weekDay = Utils.SATURDAY;
            }

            myToolbar.setTitle(new SimpleDateFormat("HH:mm").format(currentTrack.getDate()) + "   " + weekDay);
        } else {
            myToolbar.setTitle(new SimpleDateFormat("HH:mm").format(currentTrack.getDate()) + "   " + new SimpleDateFormat("MM-dd-yyyy").format(currentTrack.getDate()));
        }

        // Distancia
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        if (currentTrack.getDistance() < 1000) {
            distanceInfo.setText(df.format(currentTrack.getDistance()) + " m");
        } else {
            distanceInfo.setText(df.format((currentTrack.getDistance() / 1000)) + " km");
        }

        // Duration
        if (currentTrack.getDateEnd() != null) {
            Long durationLong = currentTrack.getDateEnd().getTime() - currentTrack.getDate().getTime();
            double durationDouble;
            // duracion en minutos;
            durationDouble = durationLong / 1000 / 60;

            if (durationDouble < 60) {
                durationInfo.setText(df.format(durationDouble) + " Min");
            } else {
                double hours = durationDouble / 60;
                durationInfo.setText(df.format(hours) + " Hours");
            }
        } else {
            durationInfo.setText("");
        }



    }

    /**
     * Oculta Info y muestra el EditInfo
     * @param v
     */
    public void toggleVisibilityInfo(View v) {
        editInfo.setVisibility(View.VISIBLE);
        info.setVisibility(View.INVISIBLE);
    }

    /**
     * Actualiza campo info en la Base de datos
     */
    private void updateTrack() {

        if (!editInfo.getText().toString().equals(info.getText().toString())) {
            trackObject.put("info", editInfo.getText().toString());
            trackObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    if (e == null) {
                        Log.i("SAVE startTrack", "OK");
                        Utils.showMessage(getApplicationContext(), "Track info successfully saved");
                    } else {
                        Log.i("SAVE startTrack", "ERROR: " + e.toString());
                        Utils.showMessage(getApplicationContext(), "Sorry, error saving Track info :(");

                    }
                }
            });
        }
    }


    /**
     * Actualiza campo info en la Base de datos
     */
    private void updateTrackFavorite() {

        if (currentTrack.isFavorite()) {
            currentTrack.setFavorite(false);
            trackObject.put("favorite", false);
            actionBarMenu.findItem(R.id.track_action_favorite).setIcon(R.drawable.ic_favorite_border_black_36dp);;
        } else {
            currentTrack.setFavorite(true);
            trackObject.put("favorite", true);
            actionBarMenu.findItem(R.id.track_action_favorite).setIcon(R.drawable.ic_favorite_black_36dp);;
        }

        trackObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i("LIOTRACKS", "Update favorite");
                    //Utils.showMessage(getApplicationContext(), "Track info successfully saved");
                } else {
                    Log.i("LIOTRACKS", "ERROR: " + e.toString());
                    Utils.showMessage(getApplicationContext(), "Sorry, error saving Track favorite :(");

                }
            }
        });

    }


    /**
     *
     */
    public void toggleMapType(){
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE){
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
    }

}
