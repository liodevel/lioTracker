package com.liodevel.lioapp_1.Activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liodevel.lioapp_1.Objects.Track;
import com.liodevel.lioapp_1.Objects.TrackPoint;
import com.liodevel.lioapp_1.R;
import com.liodevel.lioapp_1.Utils.Utils;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseSession;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import io.fabric.sdk.android.Fabric;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class SplashActivity extends Activity {

    Context context;
    SharedPreferences prefs;
    ArrayList<Track> tracksOffline = new ArrayList();
    String currentTrackObjectId = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash);
        changeNotificationBar();
        context = this;

        // Facebook Utils
        try {
            ParseFacebookUtils.initialize(this);
        } catch (Exception e){
            Utils.logInfo("Error ParseFacebookUtils");
            Utils.logInfo(e.toString());
        }

        // Parse
        try {
            Parse.enableLocalDatastore(this);
            Parse.initialize(this);

        } catch (Exception e){
            Utils.logInfo("Parse initialized");
        }

        try {
            ParseInstallation.getCurrentInstallation().put("user", ParseUser.getCurrentUser());
            ParseInstallation.getCurrentInstallation().saveInBackground();
            Utils.logInfo("Installation updated");
        } catch (Exception e){
            Utils.logError("Installation: " + e.toString());
        }



        // Si hay Conexión
        if (Utils.checkConn(this)) {
            ParseSession.getCurrentSessionInBackground(new GetCallback<ParseSession>() {
                @Override
                public void done(ParseSession object, ParseException e) {
                    if (e == null) {
                        //                    Log.i("LIOTRACK", "Session: " + object.getSessionToken());
                        //                    Log.i("LIOTRACK", "Session: " + ParseUser.getCurrentUser().getUsername());
                        if (ParseUser.getCurrentUser() != null) {
                            sendTracks();

                            Utils.showMessage(SplashActivity.this, "Hi, " + ParseUser.getCurrentUser().getUsername() + "!");
                            Intent launchNextActivity;
                            launchNextActivity = new Intent(SplashActivity.this, MapActivity2.class);
                            launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(launchNextActivity);
                            finish();
                        } else {
                            Utils.logInfo("Session: " + "No session");
                            Intent launchNextActivity;
                            launchNextActivity = new Intent(SplashActivity.this, LoginActivity.class);
                            launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(launchNextActivity);
                            finish();
                        }
                    } else {
                        Utils.logInfo("Session: " + "No session");
                        Intent launchNextActivity;
                        launchNextActivity = new Intent(SplashActivity.this, LoginActivity.class);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(launchNextActivity);
                        finish();
                    }
                }
            });
        } else {

            // Sin Conexión
            Utils.logInfo("Inicio sin conexión");

            try {
                Utils.showMessage(SplashActivity.this, "Hi, " + ParseUser.getCurrentUser().getUsername() + "!");
                Intent launchNextActivity;
                launchNextActivity = new Intent(SplashActivity.this, MapActivity2.class);
                launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                launchNextActivity.putExtra("offline", "1");
                startActivity(launchNextActivity);
                finish();
            } catch (Exception e){
                Utils.showMessage(SplashActivity.this, "Error!");
                Utils.logError(e.toString());
                finish();
            }

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


    private void sendTracks(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        try {
            Gson gsonTracks = new Gson();
            String jsonTracks = sharedPrefs.getString("tracksOffline", "");
            Utils.logInfo("----" + jsonTracks);
            Type typeTracks = new TypeToken<ArrayList<Track>>() {
            }.getType();
            ArrayList<Track> tracksOffline = gsonTracks.fromJson(jsonTracks, typeTracks);
            if (tracksOffline != null) {
                // Guardar en Parse
                for (Track track : tracksOffline) {
                    final ParseObject trackObject = new ParseObject("track");
                    trackObject.put("date", track.getDate());
                    trackObject.put("offline", true);
                    trackObject.put("user", ParseUser.getCurrentUser());

                    try {
                        trackObject.save();
                        Utils.logInfo("SAVE Track OFFLINE to Parse OK");
                        currentTrackObjectId = trackObject.getObjectId();

                        Utils.logInfo("Track OFFLINE size: " + track.getLocalTrackPoints().size());

                        // TrackPoints
                        if (currentTrackObjectId.length() > 0) {
                            for (TrackPoint tp : track.getLocalTrackPoints()) {
                                tp.setObjectId(currentTrackObjectId);

                                ParseObject tpObject = new ParseObject("trackPoint");
                                tpObject.put("position", tp.getPosition());
                                tpObject.put("date", tp.getDate());
                                tpObject.put("accuracy", tp.getAccuracy());
                                tpObject.put("track", trackObject);
                                tpObject.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(com.parse.ParseException e) {
                                        if (e == null)
                                            Log.i("SAVE", "OK");
                                        else
                                            Log.i("SAVE", "ERROR: " + e.toString());
                                    }
                                });
                            }
                        }

                    } catch (Exception e) {
                        Utils.logError(e.toString());
                    }


                }
                /// limpiar tracks offline
                editor.putString("tracksOffline", "");
                editor.commit();
                Utils.logInfo("Tracks offline eliminados");

            } else {
                Utils.logInfo("No hay Tracks pendientes");
            }
        } catch (Exception e){
            Utils.logError("Error recuperando Tracks offline");
            Utils.logError(e.toString());

            /// limpiar tracks offline
            editor.putString("tracksOffline", "");
            editor.commit();
            Utils.logInfo("Tracks offline eliminados");
        }

    }
}
