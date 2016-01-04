package com.liodevel.lioapp_1.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.liodevel.lioapp_1.R;
import com.liodevel.lioapp_1.Utils.Utils;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseSession;
import com.parse.ParseUser;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        try {
            ParseFacebookUtils.initialize(this);
        } catch (Exception e){
            Log.i("LIOTRACK", "Error ParseFacebookUtils");
        }


        try {
            Parse.enableLocalDatastore(this);
            Parse.initialize(this);

        } catch (Exception e){
            Log.i("LIOTRACK", "Parse initialized");
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
                        launchNextActivity = new Intent(SplashActivity.this, MapActivity.class);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(launchNextActivity);
                    } else {
                        Log.i("LIOTRACK", "Session: " + "No session");
                        Intent launchNextActivity;
                        launchNextActivity = new Intent(SplashActivity.this, LoginActivity.class);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(launchNextActivity);
                    }
                } else {
                    Log.i("LIOTRACK", "Session: " + "No session");
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
}
