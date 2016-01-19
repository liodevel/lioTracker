package com.liodevel.lioapp_1.Activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liodevel.lioapp_1.Objects.Track;
import com.liodevel.lioapp_1.Objects.TrackPoint;
import com.liodevel.lioapp_1.R;
import com.liodevel.lioapp_1.Utils.Server;
import com.liodevel.lioapp_1.Utils.Utils;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseSession;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

public class SplashActivity extends Activity {

    SharedPreferences prefs;
    ArrayList<Track> tracksOffline = new ArrayList();
    String currentTrackObjectId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        changeNotificationBar();

        try {
            ParseFacebookUtils.initialize(this);
        } catch (Exception e){
            Utils.logInfo("Error ParseFacebookUtils");
        }


        try {
            Parse.enableLocalDatastore(this);
            Parse.initialize(this);

        } catch (Exception e){
            Utils.logInfo("Parse initialized");
        }






        ParseSession.getCurrentSessionInBackground(new GetCallback<ParseSession>() {
            @Override
            public void done(ParseSession object, ParseException e) {
                if (e == null) {
//                    Log.i("LIOTRACK", "Session: " + object.getSessionToken());
//                    Log.i("LIOTRACK", "Session: " + ParseUser.getCurrentUser().getUsername());
                    if (ParseUser.getCurrentUser() != null) {
                        Utils.showMessage(SplashActivity.this, "Hi, " + ParseUser.getCurrentUser().getUsername() + "!");
                        Intent launchNextActivity;
                        launchNextActivity = new Intent(SplashActivity.this, MapActivity2.class);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(launchNextActivity);
                    } else {
                        Utils.logInfo("Session: " + "No session");
                        Intent launchNextActivity;
                        launchNextActivity = new Intent(SplashActivity.this, LoginActivity.class);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(launchNextActivity);
                    }
                } else {
                    Utils.logInfo("Session: " + "No session");
                    Intent launchNextActivity;
                    launchNextActivity = new Intent(SplashActivity.this, LoginActivity.class);
                    launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(launchNextActivity);
                }
            }
        });


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

    public boolean checkConn() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }


    private void sendTracks(){
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Gson gsonNoticias = new Gson();
        String jsonNoticias = this.prefs.getString("tracksOffline", "");
        Type typeNoticias = new TypeToken<ArrayList<Track>>() {
        }.getType();
        ArrayList<Track> tracksOffline = gsonNoticias.fromJson(jsonNoticias, typeNoticias);
        if (tracksOffline != null) {
            // Guardar en Parse
            for (Track track:tracksOffline){
                final ParseObject dataObject = new ParseObject("track");
                dataObject.put("date", track.getDate());
                dataObject.put("user", ParseUser.getCurrentUser());
                dataObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(com.parse.ParseException e) {
                        if (e == null) {
                            Utils.logInfo("SAVE startTrack OK");
                            currentTrackObjectId = dataObject.getObjectId();
                        } else {
                            Utils.logInfo("SAVE startTrack ERROR: " + e.toString());
                        }
                    }
                });
                if (currentTrackObjectId.length() > 0) {
                    for (TrackPoint tr : track.getLocalTrackPoints()) {
                        tr.setObjectId(currentTrackObjectId);
                        Server.sendTrackPoint(tr);
                    }
                } else {
                    // Algo ha ido mal guardando el Track
                }
            }

        } else {
            // Esta vacio
        }

    }
}
