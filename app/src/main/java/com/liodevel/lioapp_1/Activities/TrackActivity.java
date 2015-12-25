package com.liodevel.lioapp_1.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.liodevel.lioapp_1.Objects.Track;
import com.liodevel.lioapp_1.Objects.TrackPoint;
import com.liodevel.lioapp_1.R;
import com.liodevel.lioapp_1.Utils.Server;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TrackActivity extends AppCompatActivity {

    private GoogleMap mMap;
    Menu actionBarMenu;
    Marker marker;
    MarkerOptions markerOptions;
    ProgressDialog progressDialog;
    public static Context context;
    String trackObjectId = "";

    static ArrayList<TrackPoint> trackPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = this;

        Toolbar myToolbar = (Toolbar) findViewById(R.id.track_toolbar);
        setSupportActionBar(myToolbar);

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
                Toast.makeText(getApplicationContext(),"Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
            }
        };


        getTrackByObjectId(trackObjectId);
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
        boolean ret = false;
        LatLng prevPos = null;
        LatLng actualPos = null;
        ParseObject trackObject = null;

        ParseQuery<ParseObject> queryTrackObject = ParseQuery.getQuery("track");
        queryTrackObject.whereEqualTo("objectId", objectId);
        try {
            List <ParseObject> parseQueriesTrackObject = queryTrackObject.find();
            trackObject = parseQueriesTrackObject.get(0);
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
        // progressDialog.hide();
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

}
