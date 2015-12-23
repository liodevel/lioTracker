package com.liodevel.lioapp_1.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
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
        setContentView(R.layout.activity_map);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = this;

        Toolbar myToolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(myToolbar);

        //progressDialog.show(this, "Track", "Downloading track", true);
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            trackObjectId = "";
        } else {
            trackObjectId = extras.getString("objectId");
            Log.i("LIOTRACKS", "ObjectId Track: " + trackObjectId);
        }
        getTrackByObjectId(trackObjectId);

        try {
            // Loading map
            initMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    /**
     * Inicializar Mapa
     */
    public void initMap() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap == null) {
                Toast.makeText(getApplicationContext(),"Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
            }
        };

    }


    /**
     * Dibuja una linea en el mapa
     * @param start
     * @param end
     */
    public void drawTrackPoint(LatLng start, LatLng end){
        PolylineOptions line =
                new PolylineOptions().add(start, end)
                        .width(8).color(Color.RED);
        mMap.addPolyline(line);
    }

    public boolean getTrackByObjectId(String objectId){
        Log.i("LIOTRACK", "getTrackByObjectId()");
        boolean ret = false;
        LatLng prevPos = null;
        LatLng actualPos = null;
        ParseObject track = ParseObject.create("track");
        track.put("objectId", objectId);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("trackPoints");
        query.whereEqualTo("track", track);
        try {
            List <ParseObject> parseQueries = query.find();
            for (ParseObject parseObject : parseQueries) {
                Log.i("LIOTRACK", "-----");
                TrackPoint trackPoint = new TrackPoint();
                trackPoint.setObjectId(parseObject.getObjectId());
                trackPoint.setDate((Date) parseObject.get("date"));
                trackPoint.setPosition((ParseGeoPoint) parseObject.get("position"));
                trackPoints.add(trackPoint);
                actualPos = new LatLng(trackPoint.getPosition().getLatitude(), trackPoint.getPosition().getLongitude());
                if (prevPos != null) {
                    drawTrackPoint(prevPos, actualPos);
                }
                prevPos = actualPos;
            }
            ret = true;
        } catch (ParseException e) {
            Log.i("LIOTRACK", "Error: " + e.toString());
            ret = false;
        }
       // progressDialog.hide();
        return ret;
    }

}
