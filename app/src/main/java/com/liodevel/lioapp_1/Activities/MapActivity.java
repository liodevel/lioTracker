package com.liodevel.lioapp_1.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.liodevel.lioapp_1.Objects.TrackPoint;
import com.liodevel.lioapp_1.R;
import com.liodevel.lioapp_1.Utils.Server;
import com.liodevel.lioapp_1.Utils.Utils;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends AppCompatActivity {

    private GoogleMap mMap;
    Button startTrackButton;
    Menu actionBarMenu;
    Marker marker;
    MarkerOptions markerOptions;

    private boolean tracking = false;
    private boolean trackerReady = false;
    private boolean centerMap = true;
    private Boolean exit = false;
    private String currentTrackObjectId = "";
    ParseObject currentTrack = null;


    Location prevLocation;
    Location lastLocation;
    float currentTrackDistance = 0;

    LocationManager locationManager;
    LocationListener locationListener;

    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(myToolbar);

        try {
            // Inicializar mapa
            initMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

        startTrackButton = (Button) findViewById(R.id.buttonStart);
        startTrackButton.setBackgroundColor(Color.argb(100, 20, 175, 20));
        startTrackButton.setText("Getting Location...");

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!trackerReady){
            startTrackButton = (Button) findViewById(R.id.buttonStart);
            startTrackButton.setBackgroundColor(Color.argb(100, 20, 175, 20));
            startTrackButton.setText("Getting Location...");
            if (actionBarMenu != null){
                actionBarMenu.findItem(R.id.map_action_start_track).setVisible(false);
                actionBarMenu.findItem(R.id.map_action_center_map).setVisible(false);
            }

        } else {
            startTrackButton.setBackgroundColor(Color.argb(255, 20, 175, 20));
            startTrackButton.setText("Ready");
            if (actionBarMenu != null){
                actionBarMenu.findItem(R.id.map_action_start_track).setVisible(true);
                actionBarMenu.findItem(R.id.map_action_center_map).setVisible(true);
            }
        }
        if (tracking){
            startTrackButton.setBackgroundColor(Color.argb(100, 175, 20, 20));
            startTrackButton.setText("Tracking...");
            actionBarMenu.findItem(R.id.map_action_start_track).setVisible(true);
            actionBarMenu.findItem(R.id.map_action_center_map).setVisible(true);
        }
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (Exception e){

        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!trackerReady){
            startTrackButton = (Button) findViewById(R.id.buttonStart);
            startTrackButton.setBackgroundColor(Color.argb(100, 20, 175, 20));
            startTrackButton.setText("Getting Location...");
            if (actionBarMenu != null) {
                actionBarMenu.findItem(R.id.map_action_start_track).setVisible(false);
                actionBarMenu.findItem(R.id.map_action_center_map).setVisible(false);
            }

        } else {
            startTrackButton.setBackgroundColor(Color.argb(255, 20, 175, 20));
            startTrackButton.setText("Ready");
            if (actionBarMenu != null) {
                actionBarMenu.findItem(R.id.map_action_start_track).setVisible(true);
                actionBarMenu.findItem(R.id.map_action_center_map).setVisible(true);
            }
        }
        if (tracking){
            startTrackButton.setBackgroundColor(Color.argb(100, 175, 20, 20));
            startTrackButton.setText("Tracking...");
            actionBarMenu.findItem(R.id.map_action_start_track).setVisible(true);
            actionBarMenu.findItem(R.id.map_action_center_map).setVisible(true);
        }
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (Exception e){

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            timerTask.cancel();
        } catch (Exception e){

        }
        try {
            locationManager.removeUpdates(locationListener);
        } catch (Exception e){

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // START/STOP TRACKING
            case R.id.map_action_start_track:
                clickStart(null);
                return true;

            // CENTRAR MAPA
            case R.id.map_action_center_map:
                if (centerMap){
                    centerMap = false;
                    actionBarMenu.findItem(R.id.map_action_center_map).setIcon(R.drawable.abc_btn_radio_to_on_mtrl_000);
                } else {
                    centerMap();
                    centerMap = true;
                    actionBarMenu.findItem(R.id.map_action_center_map).setIcon(R.drawable.abc_btn_radio_to_on_mtrl_015);
                }
                return true;

            // MY TRACKS
            case R.id.map_action_my_tracks:
                Intent launchNextActivity;
                launchNextActivity = new Intent(MapActivity.this, MyTracksActivity.class);
                try {
                    locationManager.removeUpdates(locationListener);
                } catch (Exception e){

                }
                startActivity(launchNextActivity);
                return true;

            // LOGOUT
            case R.id.map_action_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                        .setMessage("Confirm Logout")
                        .setPositiveButton("Yes",  new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                ParseUser.getCurrentUser().logOut();
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

            // SETTINS
            /*case R.id.map_action_settings:

                return true;
*/
            default:
                return super.onOptionsItemSelected(item);

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionBarMenu = menu;
        getMenuInflater().inflate(R.menu.menu_actionbar_map, menu);
        actionBarMenu.findItem(R.id.map_action_start_track).setVisible(false);
        actionBarMenu.findItem(R.id.map_action_center_map).setVisible(false);
        actionBarMenu.findItem(R.id.map_action_profile_name).setTitle(ParseUser.getCurrentUser().getUsername());
        //actionBarMenu.findItem(R.id.map_action_profile_name).getActionView().setBackgroundColor(getResources().getColor(R.color.dark_grey));

        return true;
    }
    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Utils.showMessage(this, "Press Back again to Exit");
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
    }


    /**
     * Inicializar Mapa
     */
    public void initMap() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap == null) {
                Utils.showMessage(getApplicationContext(), "Sorry! unable to create maps");
            }
        };

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                lastLocation = location;

                if (lastLocation != null) {
                    trackerReady = true;
                    actionBarMenu.findItem(R.id.map_action_start_track).setVisible(true);
                    actionBarMenu.findItem(R.id.map_action_center_map).setVisible(true);
                    if (marker == null) {
                        markerOptions = new MarkerOptions()
                                .position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                                .title("Hi!")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        marker = mMap.addMarker(markerOptions);
                    } else {
                        marker.setPosition(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
                    }

                    if (tracking){
                        marker.setIcon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    } else {
                        marker.setIcon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        startTrackButton.setBackgroundColor(Color.argb(255, 20, 175, 20));
                        startTrackButton.setText("Ready");
                    }

                    if (centerMap) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 16)
                        );
                    }
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    /**
     * Start Button
     */
    public void clickStart(View view) {
        Log.i("CLICK", "clickStart");
        if (!tracking) {
            // START TRACKING
            if (startTrack() == 0) {
                // Start track correcto
                startTrackButton.setBackgroundColor(Color.argb(100, 175, 20, 20));
                startTrackButton.setText("Tracking...");
                tracking = true;
                currentTrackDistance = 0;

                // Animacion
                LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ImageView iv = (ImageView) inflater.inflate(R.layout.start_track_image, null);
                Animation rotation = AnimationUtils.loadAnimation(this, R.anim.tracking_animation);
                rotation.setRepeatCount(Animation.INFINITE);
                iv.startAnimation(rotation);
                actionBarMenu.findItem(R.id.map_action_start_track).setActionView(iv);

            } else {
                // NO Start track
            }
        } else {
            // STOP TRACKING
            stopTimerTrack();

            currentTrack.put("dateEnd", new Date(System.currentTimeMillis()));
            currentTrack.put("distance", currentTrackDistance);
            currentTrack.saveInBackground(new SaveCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    if (e == null) {
                        Log.i("SAVE startTrack", "OK");
                    } else {
                        Log.i("SAVE startTrack", "ERROR: " + e.toString());
                    }
                }
            });

            startTrackButton.setBackgroundColor(Color.argb(255, 20, 175, 20));
            startTrackButton.setText("Ready");
            tracking = false;

            actionBarMenu.findItem(R.id.map_action_start_track).getActionView().clearAnimation();
            actionBarMenu.findItem(R.id.map_action_start_track).setActionView(null);

        }
    }





    /**
     * Centrar Mapa
     */
    private void centerMap(){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),
                        mMap.getCameraPosition().zoom)
        );
    }

    /**
     * Dibuja una linea en el mapa
     * @param start
     * @param end
     */
    private void drawTrackPoint(LatLng start, LatLng end){
        PolylineOptions line =
                new PolylineOptions().add(start, end)
                        .width(10).color(Color.BLACK);
        mMap.addPolyline(line);
    }

    // TIMERTRACK
    /**
     * Start the TimerTask
     */
    public void startTimerTrack() {
        Log.i("StartTimer", "Track");
        timer = new Timer();
        initializeTimerTrack();
        timer.schedule(timerTask, 0, 5000); //
    }

    /**
     * Stop the TimerTask
     */
    public void stopTimerTrack() {
        Log.i("StopTimer", "Track");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * TimerTask for Tracking
     */
    public void initializeTimerTrack() {
        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        Log.i("LIOTRACK", "Sending");
                        sendLocation();
                    }
                });
            }
        };
    }

    /**
     * Send startTrack
     */
    int startTrack() {
        Log.i("SEND", "startTrack()");
        int ret = -1;
        mMap.clear();
        markerOptions = new MarkerOptions()
                .position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                .title("Hi!")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

        marker = mMap.addMarker(markerOptions);
        final ParseObject dataObject = new ParseObject("track");
        dataObject.put("date", new Date(System.currentTimeMillis()));
        dataObject.put("user", ParseUser.getCurrentUser());
        dataObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if (e == null) {
                    Log.i("SAVE startTrack", "OK");
                    currentTrack = dataObject;
                    currentTrackObjectId = dataObject.getObjectId();
                    startTimerTrack();

                } else {
                    Log.i("SAVE startTrack", "ERROR: " + e.toString());
                }
            }
        });

        ret = 0;
        return ret;
    }

    /**
     * Send location
     */
    void sendLocation() {
        Log.i("SEND", "sendLocation()");

        if (lastLocation != null) {
            TrackPoint tr = new TrackPoint();
            tr.setDate(new Date(System.currentTimeMillis()));
            tr.setPosition(new ParseGeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude()));
            tr.setAccuracy(lastLocation.getAccuracy());
            tr.setTrack(currentTrack);
            Server.sendTrackPoint(tr);

            if (prevLocation != null){
                currentTrackDistance = currentTrackDistance + prevLocation.distanceTo(lastLocation);
                drawTrackPoint(
                        new LatLng(prevLocation.getLatitude(), prevLocation.getLongitude()),
                        new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
            }
            prevLocation = lastLocation;
        } else {
            Log.i("SEND", "NULL Location");
        }
    }

}
