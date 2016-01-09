package com.liodevel.lioapp_1.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity2 extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Boolean exit = false;

    // VIEW
    private TextView textInfo, textProviderInfo;
    private Menu actionBarMenu;
    private Context context;

    // MAPS
    private GoogleMap mMap;
    private Marker marker;
    private MarkerOptions markerOptions;
    private boolean centerMap = true;

    // TRACKING
    private boolean tracking = false;
    private boolean trackerReady = false;
    private String currentTrackObjectId = "";
    private ParseObject currentTrack = null;
    private Location prevLocation;
    private Location lastLocation;
    private float currentTrackDistance = 0;
    private float trackPointDistance = 0;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Date lastTrackPointDate = new Date();
    TrackPoint previousTr = new TrackPoint();

    private boolean gps_enabled = false;
    private boolean network_enabled = false;

    // TIMER
    private Timer timer;
    private TimerTask timerTask;
    private final Handler handler = new Handler();

    // PREFERENCIAS
    private boolean onlyGPS = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.bringToFront();
        navigationView.requestLayout();
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        onlyGPS = prefs.getBoolean("only_gps", true);

        // Inicializar mapa
        try {
            initMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        textInfo = (TextView) findViewById(R.id.text_info);
        textInfo.setBackgroundColor(getResources().getColor(R.color.liodevel_dark_grey));
        textProviderInfo = (TextView) findViewById(R.id.text_provider_info);

        textInfo.setText(getResources().getString(R.string.getting_location));
        updateGpsProviders();
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Utils.logInfo("Nav item: " + id );

        // MY TRACKS
         if (id == R.id.nav_my_tracks) {

             Intent launchNextActivity;
             launchNextActivity = new Intent(MapActivity2.this, MyTracksActivity.class);
             try {
                 locationManager.removeUpdates(locationListener);
             } catch (Exception e){}
             DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
             drawer.closeDrawer(GravityCompat.START);
             startActivity(launchNextActivity);
             return true;

        } else if (id == R.id.nav_settings) {
             Intent launchSettingsActivity;
             launchSettingsActivity = new Intent(MapActivity2.this, SettingsActivity.class);
             try {
                 locationManager.removeUpdates(locationListener);
             } catch (Exception e){}
             DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
             drawer.closeDrawer(GravityCompat.START);
             startActivity(launchSettingsActivity);
             return true;


        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        onlyGPS = prefs.getBoolean("only_gps", true);

        updateViews();
        updateGpsProviders();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        onlyGPS = prefs.getBoolean("only_gps", true);

        updateViews();
        updateGpsProviders();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            timerTask.cancel();
        } catch (Exception e){
            Log.e("LIOTRACKS", "Error: " + e.toString());
        }
        try {
            currentTrack.put("dateEnd", lastTrackPointDate);
            currentTrack.put("distance", currentTrackDistance);
            currentTrack.saveInBackground(new SaveCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    if (e == null) {
                        Utils.logInfo("SAVE startTrack OK");
                        Utils.showMessage(getApplicationContext(), getResources().getString(R.string.track_saved));
                    } else {
                        Utils.logInfo("SAVE startTrack ERROR: " + e.toString());
                        Utils.showMessage(getApplicationContext(), getResources().getString(R.string.error_saving_track));

                    }
                }
            });
        } catch (Exception e){
            Utils.logError(e.toString());
        }
        try {
            locationManager.removeUpdates(locationListener);
        } catch (Exception e){
            Utils.logError(e.toString());
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
                    actionBarMenu.findItem(R.id.map_action_center_map).setIcon(R.drawable.ic_action_center_ko);
                } else {
                    centerMap();
                    centerMap = true;
                    actionBarMenu.findItem(R.id.map_action_center_map).setIcon(R.drawable.ic_action_center_ok);
                }
                return true;

            // TIPO MAPA
            case R.id.map_action_type_map:
                toggleMapType();
                return true;


            // LOGOUT
            case R.id.map_action_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                        .setMessage(getResources().getString(R.string.confirm_logout))
                        .setPositiveButton(getResources().getString(R.string.yes),  new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                ParseUser.getCurrentUser().logOut();
                                finish();
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionBarMenu = menu;
        getMenuInflater().inflate(R.menu.menu_actionbar_map, menu);
        actionBarMenu.findItem(R.id.map_action_start_track).setVisible(false);
        actionBarMenu.findItem(R.id.map_action_center_map).setVisible(false);
//        actionBarMenu.findItem(R.id.map_action_profile_name).setTitle(ParseUser.getCurrentUser().getUsername());

        return true;
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (exit) {
                finish(); // finish activity
            } else {
                Utils.showMessage(getApplicationContext(), getResources().getString(R.string.press_again_to_exit));
                exit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exit = false;
                    }
                }, 3 * 1000);
            }
        }
    }


    /**
     * Inicializar Mapa
     */
    private void initMap() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap == null) {
                Utils.showMessage(getApplicationContext(), getResources().getString(R.string.unable_to_create_map));
            }
        }

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                lastLocation = location;

                if (lastLocation != null) {
                    trackerReady = true;
                    actionBarMenu.findItem(R.id.map_action_start_track).setVisible(true);
                    actionBarMenu.findItem(R.id.map_action_center_map).setVisible(true);
                    if (mMap != null) {

                        if (marker == null) {
                            markerOptions = new MarkerOptions()
                                    .position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                                    .title("Hi!")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_green));
                            marker = mMap.addMarker(markerOptions);
                        } else {
                            marker.setPosition(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
                        }

                        if (tracking) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_red));
                        } else {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_green));
                            textInfo.setBackgroundColor(getResources().getColor(R.color.liodevel_dark_green));
                            textInfo.setText(getResources().getString(R.string.ready));

                        }

                        if (centerMap) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),
                                            mMap.getCameraPosition().zoom)
                            );
                        }
                    }
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };

        updateGpsProviders();

    }

    /**
     * Start Button
     */
    public void clickStart(View view) {
        Utils.logInfo("CLIC clickStart");
        if (!tracking) {
            // START TRACKING
            if (startTrack() == 0) {
                // Start track correcto
                textInfo.setBackgroundColor(getResources().getColor(R.color.liodevel_red));
                textInfo.setText(getResources().getString(R.string.tracking));
                tracking = true;
                currentTrackDistance = 0;

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),
                                18)
                );
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
            // STOP TRACKING

        } else {
            stopTimerTrack();

            currentTrack.put("dateEnd", lastTrackPointDate);
            currentTrack.put("distance", currentTrackDistance);
            currentTrack.saveInBackground(new SaveCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    if (e == null) {
                        Utils.logInfo("SAVE startTrack OK");
                        Utils.showMessage(getApplicationContext(), getResources().getString(R.string.track_saved));
                    } else {
                        Utils.logInfo("SAVE startTrack ERROR: " + e.toString());
                        Utils.showMessage(getApplicationContext(), getResources().getString(R.string.error_saving_track));

                    }
                }
            });

            textInfo.setBackgroundColor(getResources().getColor(R.color.liodevel_dark_green));
            textInfo.setText(getResources().getString(R.string.ready));
            tracking = false;

            actionBarMenu.findItem(R.id.map_action_start_track).getActionView().clearAnimation();
            actionBarMenu.findItem(R.id.map_action_start_track).setActionView(null);

        }
    }





    /**
     * Centrar Mapa
     */
    private void centerMap(){
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),
                            mMap.getCameraPosition().zoom)
            );
        }
    }

    /**
     * Dibuja una linea en el mapa
     * @param start Coordenadas inicio
     * @param end Coordenadas final
     * @param speed velocidad en KM/H
     * @param vehicle 1-Coche; 2-Moto; 3-Bici; 4-Patinete; 5-Andando
     */
    private void drawTrackPoint(LatLng start, LatLng end, double speed, int vehicle) {
        int colorTrack;

        if (vehicle == 1 || vehicle == 2) {
            if (speed < 10) {
                colorTrack = Color.BLACK;
            } else if (speed < 20) {
                colorTrack = Color.RED;
            } else if (speed < 30) {
                colorTrack = Color.YELLOW;
            } else if (speed < 60) {
                colorTrack = Color.GREEN;
            } else if (speed < 90) {
                colorTrack = Color.CYAN;
            } else if (speed < 120) {
                colorTrack = Color.BLUE;
            } else {
                colorTrack = Color.MAGENTA;
            }
        } else {
            if (speed < 10) {
                colorTrack = Color.BLACK;
            } else if (speed < 20) {
                colorTrack = Color.RED;
            } else if (speed < 30) {
                colorTrack = Color.YELLOW;
            } else if (speed < 60) {
                colorTrack = Color.GREEN;
            } else if (speed < 90) {
                colorTrack = Color.CYAN;
            } else if (speed < 120) {
                colorTrack = Color.BLUE;
            } else {
                colorTrack = Color.MAGENTA;
            }
        }

        if (mMap != null) {
            PolylineOptions line =
                    new PolylineOptions().add(start, end)
                            .width(12).color(colorTrack);
            mMap.addPolyline(line);
        }
    }

    // TIMERTRACK
    /**
     * Start the TimerTask
     */
    private void startTimerTrack() {
        Utils.logInfo("startTimerTrack");
        timer = new Timer();
        initializeTimerTrack();
        timer.schedule(timerTask, 0, 5000); //
    }

    /**
     * Stop the TimerTask
     */
    private void stopTimerTrack() {
        Utils.logInfo("stopTimerTrack");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * TimerTask for Tracking
     */
    private void initializeTimerTrack() {
        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        Utils.logInfo("Sending location");
                        sendLocation();
                    }
                });
            }
        };
    }

    /**
     * Send startTrack
     */
    private int startTrack() {
        Utils.logInfo("SEND startTrack()");
        int ret = -1;
        mMap.clear();
        currentTrackDistance = 0;
        markerOptions = new MarkerOptions()
                .position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                .title("Hi!")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_red)
                );

        if (mMap != null) {
            marker = mMap.addMarker(markerOptions);
        }
        final ParseObject dataObject = new ParseObject("track");
        dataObject.put("date", new Date(System.currentTimeMillis()));
        dataObject.put("user", ParseUser.getCurrentUser());
        dataObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if (e == null) {
                    Utils.logInfo("SAVE startTrack OK");
                    currentTrack = dataObject;
                    currentTrackObjectId = dataObject.getObjectId();
                    startTimerTrack();

                } else {
                    Utils.logInfo("SAVE startTrack ERROR: " + e.toString());
                }
            }
        });

        ret = 0;
        return ret;
    }

    /**
     * Enviar trackPoint
     */
    private void sendLocation() {
        Utils.logInfo("SEND sendLocation()");

        if (lastLocation != null) {
            TrackPoint tr = new TrackPoint();
            tr.setDate(new Date(System.currentTimeMillis()));
            tr.setPosition(new ParseGeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude()));
            tr.setAccuracy(lastLocation.getAccuracy());
            tr.setTrack(currentTrack);

            if (prevLocation != null){

                trackPointDistance = prevLocation.distanceTo(lastLocation);

                if (trackPointDistance > 1) {
                    Utils.logInfo("SendTrackPoint " + trackPointDistance + "m");
                    Server.sendTrackPoint(tr);
                    lastTrackPointDate = tr.getDate();

                    currentTrackDistance = currentTrackDistance + trackPointDistance;
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);
                    if (currentTrackDistance < 1000) {
                        textInfo.setText(df.format(currentTrackDistance) + " m");
                    } else {
                        textInfo.setText(df.format((currentTrackDistance / 1000)) + " km");
                    }


                    double kilometers = currentTrackDistance / 1000.0;
                    Utils.logInfo("TRACKPOINT DISTANCE      :" + trackPointDistance);
                    Utils.logInfo("TRACKPOINT DATE          :" + tr.getDate().getTime());
                    Utils.logInfo("PREVIOUSTRACKPOINT DATE  :" + previousTr.getDate().getTime());
                    long microsecs = (tr.getDate().getTime() - previousTr.getDate().getTime());
                    double hours = microsecs / 1000.0 / 3600.0;
                    double speed = kilometers / hours;
                    Utils.logInfo(speed + "km/h");

                    drawTrackPoint(
                            new LatLng(prevLocation.getLatitude(), prevLocation.getLongitude()),
                            new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),
                            speed, 1);

                    previousTr = tr;

                } else {
                    Utils.logInfo("SendTrackPoint Skipped: " + trackPointDistance + "m");
                }

            } else {
                Utils.logInfo("FIRST SendTrackPoint");
                Server.sendTrackPoint(tr);
                lastTrackPointDate = tr.getDate();
                previousTr = tr;

            }
            prevLocation = lastLocation;

        } else {
            Utils.logInfo("SEND NULL Location");
        }
    }

    /**
     * Actualiza info de localización
     */
    private void updateGpsProviders() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (Exception e) {}
        if (onlyGPS) {
            textProviderInfo.setText(getResources().getString(R.string.GPS));
        } else {
            textProviderInfo.setText(getResources().getString(R.string.gps_network));
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            } catch (Exception e) {
            }

        }

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(getApplicationContext().getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(getApplicationContext().getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(myIntent);
                }
            });
            dialog.setNegativeButton(getApplicationContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    finish();
                }
            });
            dialog.show();
        }

    }

    /**
     * Actualiza elementos de la pantalla al volver a esta
     */
    private void updateViews(){
        if (!trackerReady){
            textInfo.setBackgroundColor(getResources().getColor(R.color.liodevel_dark_grey));
            textInfo.setText(getResources().getString(R.string.getting_location));
            if (actionBarMenu != null) {
                actionBarMenu.findItem(R.id.map_action_start_track).setVisible(false);
                actionBarMenu.findItem(R.id.map_action_center_map).setVisible(false);
            }

        } else {
            textInfo.setBackgroundColor(getResources().getColor(R.color.liodevel_dark_green));
            textInfo.setText(getResources().getString(R.string.ready));
            if (actionBarMenu != null) {
                actionBarMenu.findItem(R.id.map_action_start_track).setVisible(true);
                actionBarMenu.findItem(R.id.map_action_center_map).setVisible(true);
            }
        }
        if (tracking){
            textInfo.setBackgroundColor(getResources().getColor(R.color.liodevel_red));
            textInfo.setText(getResources().getString(R.string.tracking));
            actionBarMenu.findItem(R.id.map_action_start_track).setVisible(true);
            actionBarMenu.findItem(R.id.map_action_center_map).setVisible(true);
        }

    }

    private void toggleMapType(){
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE){
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
    }


}