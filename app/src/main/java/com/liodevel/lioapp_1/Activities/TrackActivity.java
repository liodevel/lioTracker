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
import android.view.WindowManager;
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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;;

public class TrackActivity extends AppCompatActivity {

    private GoogleMap mMap;
    Menu actionBarMenu;
    Marker marker;
    MarkerOptions markerOptions;
    ProgressDialog progressDialog;
    public static Context context;
    String trackObjectId = "";
    Track currentTrack = new Track();
    static ProgressDialog progress;

    Toolbar myToolbar;
    TextView durationInfo, distanceInfo;

    static ArrayList<TrackPoint> trackPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = this;

        myToolbar = (Toolbar) findViewById(R.id.track_toolbar);
        setSupportActionBar(myToolbar);
        durationInfo = (TextView)findViewById(R.id.text_track_duration_track_info);
        distanceInfo = (TextView)findViewById(R.id.text_track_distance_track_info);

        //progressDialog.show(this, "Track", "Downloading track", true);
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            trackObjectId = "";
        } else {
            trackObjectId = extras.getString("objectId");
            Log.i("LIOTRACKS", "ObjectId Track: " + trackObjectId);
        }

        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapTrack)).getMap();
            if (mMap == null) {
                Utils.showMessage(getApplicationContext(),"Sorry! unable to create maps");
            }
        };


        getTrackByObjectId(trackObjectId);
        updateTrackInfo();




    }



    /**
     * Dibuja una linea en el mapa
     * @param start
     * @param end
     */
    public void drawTrackPoint(LatLng start, LatLng end){
        PolylineOptions line =
                new PolylineOptions().add(start, end)
                        .width(10).color(Color.BLACK);
        mMap.addPolyline(line);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionBarMenu = menu;
        getMenuInflater().inflate(R.menu.menu_actionbar_track, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_action_delete_track:
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder
                        .setMessage("Delete track")
                        .setPositiveButton("Yes",  new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                deleteTrackByObjectId(trackObjectId);
                                finish();
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


    public boolean getTrackByObjectId(String objectId){
        Log.i("LIOTRACK", "getTrackByObjectId()");

        progress = new ProgressDialog(context);
        progress.setMessage("Loading track");
        progress.show();

        boolean ret = false;
        LatLng prevPos = null;
        LatLng actualPos = null;
        ParseObject trackObject = null;

        ParseQuery<ParseObject> queryTrackObject = ParseQuery.getQuery("track");
        queryTrackObject.whereEqualTo("objectId", objectId);
        try {
            List <ParseObject> parseQueriesTrackObject = queryTrackObject.find();
            trackObject = parseQueriesTrackObject.get(0);
            currentTrack.setObjectId(parseQueriesTrackObject.get(0).getObjectId());
            currentTrack.setDate((Date) parseQueriesTrackObject.get(0).get("date"));
            currentTrack.setDateEnd((Date) parseQueriesTrackObject.get(0).get("dateEnd"));
            currentTrack.setDistance((float)parseQueriesTrackObject.get(0).getDouble("distance"));
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
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(actualPos, 16));
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

    public void deleteTrackByObjectId(String objectId){
        Log.i("LIOTRACK", "deleteTrackByObjectId()");
        ParseObject trackObject = null;

        ParseQuery<ParseObject> queryTrackObject = ParseQuery.getQuery("track");
        queryTrackObject.whereEqualTo("objectId", objectId);
        try {
            List <ParseObject> parseQueriesTrackObject = queryTrackObject.find();
            trackObject = parseQueriesTrackObject.get(0);
            trackObject.delete();
            trackObject.saveInBackground();
            Log.i("LIOTRACK", "Track ID: " + trackObject.getObjectId());

        } catch (ParseException e) {
            Log.i("LIOTRACK", "Error deleting: " + e.toString());
        }
    }

    void updateTrackInfo(){
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentTrack.getDate());

        if (currentDate.getTime() - currentTrack.getDate().getTime() < TimeUnit.MILLISECONDS.convert(6, TimeUnit.DAYS)){
            String weekDay = "";
            if (c.get(Calendar.DAY_OF_WEEK) == 1){weekDay = Utils.SATURDAY;}
            else if (c.get(Calendar.DAY_OF_WEEK) == 2){weekDay = Utils.MONDAY;}
            else if (c.get(Calendar.DAY_OF_WEEK) == 3){weekDay = Utils.TUESDAY;}
            else if (c.get(Calendar.DAY_OF_WEEK) == 4){weekDay = Utils.WEDNESDAY;}
            else if (c.get(Calendar.DAY_OF_WEEK) == 5){weekDay = Utils.THURSDAY;}
            else if (c.get(Calendar.DAY_OF_WEEK) == 6){weekDay = Utils.FRIDAY;}
            else if (c.get(Calendar.DAY_OF_WEEK) == 7){weekDay = Utils.SATURDAY;}

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
            // duracion en minutos;
            durationLong = durationLong / 1000 / 60;

            if (durationLong < 60) {
                durationInfo.setText(durationLong + " Min");
            } else {
                float hours = durationLong / 60;
                durationInfo.setText(hours + " Hours");
            }
        } else {
            durationInfo.setText("");
        }

    }

}
