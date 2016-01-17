package com.liodevel.lioapp_1.Activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

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
}
