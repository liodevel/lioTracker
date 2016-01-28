package com.liodevel.lioapp_1.Activities;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liodevel.lioapp_1.Objects.Track;
import com.liodevel.lioapp_1.Objects.TrackPoint;
import com.liodevel.lioapp_1.R;
import com.liodevel.lioapp_1.Utils.Server;
import com.liodevel.lioapp_1.Utils.Utils;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Boolean exit = false;

    // VIEW
    private TextView startButton, textProviderInfo, textDistanceInfo, userName;
    private Menu actionBarMenu;
    private Context context;
    private Chronometer chronoTrack;
    private MenuItem vehicleSpinner;

    private TextView chartBlack, chartRed, chartOrange, chartYellow, chartGreen, chartDarkGreen, chartBlue, chartCyan, chartMagenta;

    private LinearLayout leyenda1, leyenda2, leyenda3, leyenda4, leyenda5, leyendaColores;

    // MAPS
    private GoogleMap mMap;
    private Marker marker;
    private MarkerOptions markerOptions;
    private boolean centerMap = true;

    // TRACKING
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
    private int vehicle = 1;

    //OFFLINE
    private ArrayList<Track> tracksOffline = new ArrayList();
    private Track currentTrackOffline = new Track();
    private int currentTrackIndex = 0;

    // FLAGS
    private boolean tracking = false;
    private boolean trackerReady = false;
    private boolean offLineMode = false;

    private boolean gps_enabled = false;
    private boolean network_enabled = false;

    // TIMER
    private Timer timer;
    private TimerTask timerTask;
    private final Handler handler = new Handler();
    int counterTask = 0;
    long secondsTracking = 0;
    long startTimemillis;
    long currentTimemillis;

    // PREFERENCIAS POR DEFECTO
    private boolean onlyGPS = true;
    private int saveFrequency= 5;

    // NOTIFICACION
    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.logInfo("-------onCreate()");

        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = this;

        changeNotificationBar();

        // Comprobar si está en modo offline
        if (getIntent().getStringExtra("offline") != null && getIntent().getStringExtra("offline").equals("1")){
            //offLineMode = true;

        }


        // ToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.liodevel_white)));

        // Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.bringToFront();
        navigationView.requestLayout();
        navigationView.setNavigationItemSelectedListener(this);
        userName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textViewUserName);
        userName.setText(ParseUser.getCurrentUser().getUsername());

        // Shared Preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        onlyGPS = prefs.getBoolean("only_gps", true);

        try {
            int tempSaveFrequency = Integer.parseInt(prefs.getString("save_frequency", "5"));
            if (tempSaveFrequency < 5){
                saveFrequency = 5;
            } else if (tempSaveFrequency > 60){
                saveFrequency = 60;
            } else {
                saveFrequency = tempSaveFrequency;
            }

        } catch (Exception e){
            saveFrequency = 5;
        }

        Utils.logInfo("Pref_save_frequency: " + saveFrequency);

        // Inicializar mapa
        try {
            initMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Infos
        startButton = (TextView) findViewById(R.id.text_info);
        startButton.setBackgroundColor(ContextCompat.getColor(this, R.color.liodevel_dark_grey));
        textProviderInfo = (TextView) findViewById(R.id.text_provider_info);
        //textTimeInfo = (TextView) findViewById(R.id.text_time_info);
        textDistanceInfo = (TextView) findViewById(R.id.text_distance_info);
        chronoTrack = (Chronometer) findViewById(R.id.chronoTracking);

        startButton.setText(getResources().getString(R.string.getting_location));
        startButton.setTextSize(14);

        // Velocidades
        chartBlack = (TextView) findViewById((R.id.speed_black));
        chartRed = (TextView) findViewById((R.id.speed_red));
        chartOrange = (TextView) findViewById((R.id.speed_orange));
        chartYellow = (TextView) findViewById((R.id.speed_yellow));
        chartGreen = (TextView) findViewById((R.id.speed_green));
        chartDarkGreen = (TextView) findViewById((R.id.speed_dark_green));
        chartBlue = (TextView) findViewById((R.id.speed_blue));
        chartCyan = (TextView) findViewById((R.id.speed_cyan));
        chartMagenta = (TextView) findViewById((R.id.speed_magenta));

        leyenda1 = (LinearLayout) findViewById(R.id.map_leyenda_1);
        leyenda2 = (LinearLayout) findViewById(R.id.map_leyenda_2);
        leyenda3 = (LinearLayout) findViewById(R.id.map_leyenda_3);
        leyenda4 = (LinearLayout) findViewById(R.id.map_leyenda_4);
        leyenda5 = (LinearLayout) findViewById(R.id.map_leyenda_5);
        leyendaColores = (LinearLayout) findViewById(R.id.map_leyenda_colores);

        updateGpsProviders();
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Utils.logInfo("Nav item: " + id);

            // MY TRACKS
         if (id == R.id.nav_my_tracks && !tracking) {

             Intent launchNextActivity;
             launchNextActivity = new Intent(MapActivity2.this, MyTracksActivity.class);
             launchNextActivity.putExtra("favorites", "0");
             try {
                 locationManager.removeUpdates(locationListener);
             } catch (Exception e) {
             }
             DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
             drawer.closeDrawer(GravityCompat.START);
             startActivity(launchNextActivity);
             return true;

             // MY FAVORITE TRACKS
         }else if (id == R.id.nav_my_favorite_tracks && !tracking) {

             Intent launchFavoritesActivity;
             launchFavoritesActivity = new Intent(MapActivity2.this, MyTracksActivity.class);
             launchFavoritesActivity.putExtra("favorites", "1");
             try {
                 locationManager.removeUpdates(locationListener);
             } catch (Exception e){}
                 DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                 drawer.closeDrawer(GravityCompat.START);
                 startActivity(launchFavoritesActivity);
                 return true;

             // SETTINGS
        } else if (id == R.id.nav_settings) {
             Intent launchSettingsActivity;
             launchSettingsActivity = new Intent(MapActivity2.this, SettingsActivity.class);
             try {
                 locationManager.removeUpdates(locationListener);
             } catch (Exception e) {
             }
             DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
             drawer.closeDrawer(GravityCompat.START);
             startActivity(launchSettingsActivity);
             return true;

         // CAMBIAR PASSWORD
             /*
        } else if (id == R.id.nav_password) {

             Intent launchNextActivity;
             launchNextActivity = new Intent(MapActivity2.this, ChangePasswordActivity.class);
             try {
                 locationManager.removeUpdates(locationListener);
             } catch (Exception e){}
             DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
             drawer.closeDrawer(GravityCompat.START);
             startActivity(launchNextActivity);
             return true;
*/
         // LOGOUT
        } else if(id == R.id.nav_logout){
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder
                     .setMessage(getResources().getString(R.string.confirm_logout))
                     .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int id) {
                             ParseUser.getCurrentUser().logOut();
                             finish();
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

        // COMPARTIR
         }
         /* else if (id == R.id.nav_share) {

            }
        */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




    @Override
    protected void onResume() {
        Utils.logInfo("-------onResume()");
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        onlyGPS = prefs.getBoolean("only_gps", true);
        try {
            int tempSaveFrequency = Integer.parseInt(prefs.getString("save_frequency", "5"));
            if (tempSaveFrequency < 5){
                saveFrequency = 5;
            } else if (tempSaveFrequency > 60){
                saveFrequency = 60;
            } else {
                saveFrequency = tempSaveFrequency;
            }

        } catch (Exception e){
            saveFrequency = 5;
        }

        updateViews();
        updateGpsProviders();

    }

    @Override
    protected void onRestart() {
        Utils.logInfo("-------onRestart()");
        super.onRestart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        onlyGPS = prefs.getBoolean("only_gps", true);
        try {
            int tempSaveFrequency = Integer.parseInt(prefs.getString("save_frequency", "5"));
            if (tempSaveFrequency < 5){
                saveFrequency = 5;
            } else if (tempSaveFrequency > 60){
                saveFrequency = 60;
            } else {
                saveFrequency = tempSaveFrequency;
            }

        } catch (Exception e){
            saveFrequency = 5;
        }

        updateViews();
        updateGpsProviders();

    }

    @Override
    protected void onDestroy() {
        Utils.logInfo("-------onDestroy()");
        super.onDestroy();
        try {
            timerTask.cancel();
        } catch (Exception e){
            Utils.logInfo("Error: " + e.toString());
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
           /* case R.id.map_action_start_track:
                //clickStart(null);
                return true;
*/
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

            // TIPO MAPA
            case R.id.map_action_vehicle:
                if (vehicle == 1){
                    toggleVehicle(2);
                    vehicle = 2;
                } else if (vehicle == 2){
                    toggleVehicle(3);
                    vehicle = 3;
                } else if (vehicle == 3){
                    toggleVehicle(4);
                    vehicle = 4;
                } else if (vehicle == 4){
                    toggleVehicle(5);
                    vehicle = 5;
                } else if (vehicle == 5){
                    toggleVehicle(1);
                    vehicle = 1;
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionBarMenu = menu;
        getMenuInflater().inflate(R.menu.menu_actionbar_map, menu);
        //actionBarMenu.findItem(R.id.map_action_start_track).setVisible(false);
        actionBarMenu.findItem(R.id.map_action_center_map).setVisible(false);
        toggleVehicle(vehicle);


        return true;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (tracking){
                Utils.showMessage(getApplicationContext(), getResources().getString(R.string.stop_tracking));
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
                            startButton.setBackgroundColor(ContextCompat.getColor(context, R.color.liodevel_red));
                            startButton.setText(getResources().getString(R.string.ready));
                            startButton.setTextSize(30);

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
     * Start/Stop Button
     */
    public void clickStart(View view) {
        if (!tracking) {
            // START TRACKING
            if (trackerReady) {
                Utils.logInfo("START Tracking");

                startTrack();
                // Start track correcto
                startButton.setBackgroundColor(ContextCompat.getColor(this, R.color.liodevel_red));
                setInfosStart(true);

                startButton.setText(getResources().getString(R.string.push_to_stop));
                startButton.setTextSize(30);
                tracking = true;
                chronoTrack.setBase(SystemClock.elapsedRealtime());
                chronoTrack.start();
                currentTrackDistance = 0;
                textDistanceInfo.setText("0 m");

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),
                                18)
                );

            } else {
                // GPS no preparado
                Utils.logInfo("NO GPS Ready");

            }
        } else {
            // PARAR TRACKING ?
            Utils.logInfo("STOP Tracking (Dialog)");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setMessage(getResources().getString(R.string.ask_stop_tracking))
                    .setPositiveButton(getResources().getString(R.string.yes),  new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            // STOP TRACKING
                            Utils.logInfo("STOP Tracking");
                            stopTimerTrack();
                            chronoTrack.stop();

                            // Cerrar notificación
                            mNotificationManager.cancel(1);

                            if(!offLineMode) {
                                currentTrack.put("dateEnd", lastTrackPointDate);
                                currentTrack.put("distance", currentTrackDistance);
                                currentTrack.put("closed", true);
                                currentTrack.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(com.parse.ParseException e) {
                                        if (e == null) {
                                            Utils.logInfo("SAVE to PARSE startTrack OK");
                                            Utils.showMessage(getApplicationContext(), getResources().getString(R.string.track_saved));
                                        } else {
                                            Utils.logInfo("SAVE to PARSE startTrack ERROR: " + e.toString());
                                            Utils.showMessage(getApplicationContext(), getResources().getString(R.string.error_saving_track));

                                        }
                                    }
                                });


                                // OFFLINE
                            } else {

                                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                                SharedPreferences.Editor editor = sharedPrefs.edit();

                                // Recuperar Tracks en SharedPrefs
                                Gson gsonTracks = new Gson();
                                String jsonTracks = sharedPrefs.getString("tracksOffline", "");
                                Utils.logInfo("----" + jsonTracks);
                                Type typeTracks = new TypeToken<ArrayList<Track>>() {
                                }.getType();
                                tracksOffline = gsonTracks.fromJson(jsonTracks, typeTracks);
                                if (tracksOffline == null){
                                    tracksOffline = new ArrayList();
                                }
                                Utils.logInfo("Tracks recuperados de SharedPrefs: " + tracksOffline.size());

                                // Añadir currentTrack
                                //currentTrackOffline.setDateEnd(lastTrackPointDate);
                                currentTrackOffline.setDistance(currentTrackDistance);
                                currentTrackOffline.setClosed(true);
                                tracksOffline.add(currentTrackOffline);
                                currentTrackIndex++;
                                Utils.logInfo("SAVE OFFLINE Track OK");

                                /// Guardar Preferences
                                // Array de Tracks
                                Utils.logInfo("Tracks guardados en SharedPrefs: " + tracksOffline.size());
                                Gson gson = new Gson();
                                String jsonTracksOffline = gson.toJson(tracksOffline);
                                editor.putString("tracksOffline", jsonTracksOffline);
                                Log.i("--- PREFS", "tracksOffline: " + jsonTracksOffline);
                                editor.commit();
                                Utils.logInfo("SAVE OFFLINE Track OK");

                            }

                            offLineMode = false;
                            Utils.logInfo("OFFLINE MODE FALSE!");
                            startButton.setBackgroundColor(ContextCompat.getColor(context, R.color.liodevel_red));
                            startButton.setText(getResources().getString(R.string.ready));
                            startButton.setTextSize(30);
                            setInfosStart(false);
                            tracking = false;

                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.cancel();
                        }
                    })
                    .show();



        }
    }



    // TIMERTRACK
    /**
     * Start the TimerTask
     */
    private void startTimerTrack() {
        Utils.logInfo("startTimerTrack");
        secondsTracking = 0;
        startTimemillis = System.currentTimeMillis();
        timer = new Timer();
        initializeTimerTrack();
        timer.schedule(timerTask, 0, 1000); //
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

                        if (counterTask == 0) {
                            Utils.logInfo("Sending location");
                            sendLocation();
                        }
                        if (counterTask == saveFrequency-1){
                            counterTask = -1;
                        }
                        Utils.logInfo("TimerTrack: " + counterTask);
                        Utils.logInfo("TimerTrack: " + chronoTrack.getText());


                        // Actualizar Notificación
                        DecimalFormat df = new DecimalFormat();
                        df.setMaximumFractionDigits(2);
                        String notifDistance = "0.0 m";
                        if (currentTrackDistance < 1000) {
                            notifDistance = df.format(currentTrackDistance) + " m";
                        } else {
                            notifDistance = df.format((currentTrackDistance / 1000)) + " km";
                        }
                        mBuilder.setContentText(
                                Utils.secondsToHour((System.currentTimeMillis() - startTimemillis) / 1000)
                                        + " (" + notifDistance + ")");
                        mNotificationManager.notify(1, mBuilder.build());

                        counterTask++;
                        secondsTracking++;
                    }
                });
            }
        };
    }







    /**
     * Send startTrack
     */
    private int startTrack() {


        // NOTIFICACION
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_stat_icono_notificaciones);
        mBuilder.setOngoing(true);
        mBuilder.setContentTitle(getResources().getString(R.string.tracking));
        mBuilder.setContentText("---");
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

        Intent resultIntent = new Intent(this, MapActivity2.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);
        mBuilder.setContentIntent(pendingIntent);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1 , mBuilder.build());



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
        if (Utils.checkConn(context)) {
            final ParseObject dataObject = new ParseObject("track");
            dataObject.put("date", new Date(System.currentTimeMillis()));
            dataObject.put("user", ParseUser.getCurrentUser());
            dataObject.put("vehicle", vehicle);
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
        } else {
            Utils.logInfo("OFFLINE MODE!");
            offLineMode = true;
            currentTrackOffline = new Track();
            currentTrackOffline.setOffline(true);
            currentTrackOffline.setLocalId(System.currentTimeMillis());
            currentTrackOffline.setDate(new Date(System.currentTimeMillis()));
            currentTrackOffline.setUser(ParseUser.getCurrentUser());
            Utils.logInfo("SAVE OFFLINE Track OK");
            startTimerTrack();

        }

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
            tr.setProvider(lastLocation.getProvider());
            if (!offLineMode) {
                tr.setTrack(currentTrack);
            }

            if (prevLocation != null){

                trackPointDistance = prevLocation.distanceTo(lastLocation);

                if (trackPointDistance > 1) {
                    Utils.logInfo("SendTrackPoint " + trackPointDistance + "m");

                    if (!offLineMode) {
                        Server.sendTrackPoint(tr);
                    } else {
                        currentTrackOffline.getLocalTrackPoints().add(tr);
                        Utils.logInfo("OFFLINE MODE, TrackPoint OK");
                    }

                    lastTrackPointDate = tr.getDate();
                    currentTrackDistance = currentTrackDistance + trackPointDistance;
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);
                    if (currentTrackDistance < 1000) {
                        textDistanceInfo.setText(df.format(currentTrackDistance) + " m");
                    } else {
                        textDistanceInfo.setText(df.format((currentTrackDistance / 1000)) + " km");
                    }

                    double kilometers = trackPointDistance / 1000.0;
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
                            speed, vehicle);
                    previousTr = tr;

                    //updateSpeedChart(speed);


                } else {
                    Utils.logInfo("SendTrackPoint Skipped: " + trackPointDistance + "m");
                }

            } else {
                Utils.logInfo("FIRST SendTrackPoint");
                if (!offLineMode) {
                    Server.sendTrackPoint(tr);
                } else {
                    currentTrackOffline.getLocalTrackPoints().add(tr);
                    Utils.logInfo("OFFLINE MODE, TrackPoint OK");
                }
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
        } catch (Exception e) {
        }
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
            startButton.setBackgroundColor(ContextCompat.getColor(this, R.color.liodevel_dark_grey));
            setInfosStart(false);
            startButton.setText(getResources().getString(R.string.getting_location));
            startButton.setTextSize(14);
            if (actionBarMenu != null) {
                //actionBarMenu.findItem(R.id.map_action_start_track).setVisible(false);
                actionBarMenu.findItem(R.id.map_action_center_map).setVisible(false);
            }

        } else {
            startButton.setBackgroundColor(ContextCompat.getColor(this, R.color.liodevel_red));
            setInfosStart(false);
            startButton.setText(getResources().getString(R.string.ready));
            startButton.setTextSize(30);

            if (actionBarMenu != null) {
               // actionBarMenu.findItem(R.id.map_action_start_track).setVisible(false);
                actionBarMenu.findItem(R.id.map_action_center_map).setVisible(true);
            }
        }
        if (tracking){
            startButton.setBackgroundColor(ContextCompat.getColor(this, R.color.liodevel_red));
            setInfosStart(true);
            startButton.setText(getResources().getString(R.string.push_to_stop));
            startButton.setTextSize(30);
            //actionBarMenu.findItem(R.id.map_action_start_track).setVisible(true);
            actionBarMenu.findItem(R.id.map_action_center_map).setVisible(true);
        }

    }

    /**
     * Cambiar tipo de mapa
     */
    private void toggleMapType(){
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE){
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
    }


    /**
     * Cambiar tipo de Actividad / Vehículo
     * cambiar variable vehicle
     * icono en actionbar
     * adaptar gráfica de velocidades
     */
    private void toggleVehicle(int vehicle){
        Animation inFromRight = AnimationUtils.loadAnimation(this, R.anim.anim_in_from_right);
        Animation inFromLeft = AnimationUtils.loadAnimation(this, R.anim.anim_in_from_left);
        Animation outToRight = AnimationUtils.loadAnimation(this, R.anim.anim_out_to_right);

        if (vehicle == 2){
            Utils.logInfo("Vehicle -> 2");
            actionBarMenu.findItem(R.id.map_action_vehicle).setIcon(R.drawable.ic_motorcycle_black_36dp);
            leyenda2.bringToFront();
            leyendaColores.bringToFront();
            leyenda2.startAnimation(inFromLeft);
            leyenda1.startAnimation(outToRight);
        } else if(vehicle == 3){
            Utils.logInfo("Vehicle -> 3");
            actionBarMenu.findItem(R.id.map_action_vehicle).setIcon(R.drawable.ic_directions_bike_black_36dp);
            leyenda3.bringToFront();
            leyendaColores.bringToFront();
            leyenda3.startAnimation(inFromLeft);
            leyenda2.startAnimation(outToRight);

        } else if(vehicle == 4){
            Utils.logInfo("Vehicle -> 4");
            actionBarMenu.findItem(R.id.map_action_vehicle).setIcon(R.drawable.ic_directions_walk_black_36dp);
            leyenda4.bringToFront();
            leyendaColores.bringToFront();
            leyenda4.startAnimation(inFromLeft);
            leyenda3.startAnimation(outToRight);

        } else if(vehicle == 5){
            Utils.logInfo("Vehicle -> 5");
            actionBarMenu.findItem(R.id.map_action_vehicle).setIcon(R.drawable.ic_directions_run_black_36dp);
            leyenda5.bringToFront();
            leyendaColores.bringToFront();
            leyenda5.startAnimation(inFromLeft);
            leyenda4.startAnimation(outToRight);

        } else if(vehicle == 1){
            Utils.logInfo("Vehicle -> 1");
            actionBarMenu.findItem(R.id.map_action_vehicle).setIcon(R.drawable.ic_directions_car_black_36dp);
            leyenda1.bringToFront();
            leyendaColores.bringToFront();
            leyenda1.startAnimation(inFromLeft);
            leyenda5.startAnimation(outToRight);

        }
    }

    /**
     * Dibuja una linea en el mapa
     * @param start Coordenadas inicio
     * @param end Coordenadas final
     * @param speed velocidad en KM/H
     * @param vehicle 1-Coche; 2-Moto; 3-Bici; 4-Andando; 5-Corriendo
     */
    private void drawTrackPoint(LatLng start, LatLng end, double speed, int vehicle) {
        int colorTrack;

        // Coche o Moto
        if (vehicle == 1 || vehicle == 2) {
            if (speed < 10) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_black);
            } else if (speed < 20) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_red);
            } else if (speed < 30) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_orange);
            } else if (speed < 40) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_yellow);
            } else if (speed < 50) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_green);
            } else if (speed < 70) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_dark_green);
            } else if (speed < 90) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_blue);
            } else if (speed < 120) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_cyan);
            } else {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_magenta);
            }

        // Bici
        } else if (vehicle == 3) {
            if (speed < 5) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_black);
            } else if (speed < 10) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_red);
            } else if (speed < 15) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_orange);
            } else if (speed < 20) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_yellow);
            } else if (speed < 25) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_green);
            } else if (speed < 35) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_dark_green);
            } else if (speed < 45) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_blue);
            } else if (speed < 55) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_cyan);
            } else {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_magenta);
            }
        // Andando
        } else if (vehicle == 4) {
            if (speed < 2) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_black);
            } else if (speed < 4) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_red);
            } else if (speed < 6) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_orange);
            } else if (speed < 8) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_yellow);
            } else if (speed < 10) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_green);
            } else if (speed < 12) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_dark_green);
            } else if (speed < 14) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_blue);
            } else if (speed < 16) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_cyan);
            } else {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_magenta);
            }

        // Corriendo
        } else {
            if (speed < 5) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_black);
            } else if (speed < 10) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_red);
            } else if (speed < 15) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_orange);
            } else if (speed < 20) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_yellow);
            } else if (speed < 25) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_green);
            } else if (speed < 30) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_dark_green);
            } else if (speed < 35) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_blue);
            } else if (speed < 40) {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_cyan);
            } else {
                colorTrack = ContextCompat.getColor(this, R.color.liodevel_chart_magenta);
            }
        }

        if (mMap != null) {
            PolylineOptions line =
                    new PolylineOptions().add(start, end)
                            .width(12).color(colorTrack);
            mMap.addPolyline(line);
        }
    }


    @TargetApi(21)
    private void changeNotificationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utils.logInfo("Notif.Bar.Coloured");
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(this.getResources().getColor(R.color.liodevel_dark_green));
        } else {
            Utils.logInfo("Ap");
        }
    }


    public void scaleViewUp(View v) {
        Animation scale = AnimationUtils.loadAnimation(this, R.anim.speed_animation_up);
        scale.setStartOffset(5000);
        scale.setFillAfter(true);
        v.clearAnimation();
        v.setAnimation(scale);
    }


    public void scaleViewDown(View v) {
        Animation scale = AnimationUtils.loadAnimation(this, R.anim.speed_animation_down);
        scale.setStartOffset(5000);
        scale.setFillAfter(true);
        v.clearAnimation();
        v.setAnimation(scale);
    }


    public void updateSpeedChart(double speed){

        Utils.logInfo("updateSpeedChart()");
        /*
        scaleView(chartBlack, 1.5f, 1.0f);
        scaleView(chartRed, 1.5f, 1.0f);
        scaleView(chartOrange, 1.5f, 1.0f);
        scaleView(chartYellow, 1.5f, 1.0f);
        scaleView(chartGreen, 1.5f, 1.0f);
        scaleView(chartDarkGreen, 1.5f, 1.0f);
        scaleView(chartBlue, 1.5f, 1.0f);
        scaleView(chartCyan, 1.5f, 1.0f);
        scaleView(chartMagenta, 1.5f, 1.0f);
*/
        if (speed < 10) {
            chartBlack.bringToFront();
            scaleViewUp(chartBlack);
        } else if (speed < 20) {
            chartRed.bringToFront();
            scaleViewUp(chartRed);
        } else if (speed < 30) {
            chartOrange.bringToFront();
            scaleViewUp(chartOrange);
        } else if (speed < 40) {
            chartYellow.bringToFront();
            scaleViewUp(chartYellow);
        } else if (speed < 50) {
            chartGreen.bringToFront();
            scaleViewUp(chartGreen);
        } else if (speed < 70) {
            chartDarkGreen.bringToFront();
            scaleViewUp(chartDarkGreen);
        } else if (speed < 90) {
            chartBlue.bringToFront();
            scaleViewUp(chartBlue);
        } else if (speed < 120) {
            chartCyan.bringToFront();
            scaleViewUp(chartCyan);
        } else {
            chartMagenta.bringToFront();
            scaleViewUp(chartMagenta);
        }
    }


    private void setInfosStart(boolean start){
        if (start) {
            textDistanceInfo.setBackground(getResources().getDrawable(R.color.liodevel_red));
            textProviderInfo.setBackground(getResources().getDrawable(R.color.liodevel_red));
            chronoTrack.setBackground(getResources().getDrawable(R.color.liodevel_red));
        } else {
            textDistanceInfo.setBackground(getResources().getDrawable(R.color.liodevel_dark_grey));
            textProviderInfo.setBackground(getResources().getDrawable(R.color.liodevel_dark_grey));
            chronoTrack.setBackground(getResources().getDrawable(R.color.liodevel_dark_grey));
        }

    }


}
