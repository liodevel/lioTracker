package com.liodevel.lioapp_1.Utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by emilio on 29/12/15.
 */
public class Utils {


    public static final String BLACK = "ff000000";
    public static final String BLUE = "ff0000ff";
    public static final String CYAN = "ff00ffff";
    public static final String DKGRAY = "ff444444";
    public static final String GRAY = "ff888888";
    public static final String GREEN = "ff00ff00";
    public static final String LTGRAY = "ffcccccc";
    public static final String MAGENTA = "ffff00ff";
    public static final String RED = "ffff0000";
    public static final String TRANSPARENT = "00000000";
    public static final String WHITE = "ffffffff";
    public static final String YELLOW = "ffffff00";


    /**
     * Shows a Toast
     * @param context
     * @param message
     */
    public static void showMessage(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    /**
     * Shows a SnakBar
     * @param view
     * @param message
     */
    public static void showMessage(View view, String message){
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }


    public static void logInfo(String message){
        Log.i("myTracks", message);
    }
    public static void logError(String message){
        Log.e("myTracks", "ERROR: " + message);
    }


    public static String minutesToHour(double min){
        if (min < 60){
            return (Math.round(min) + "min");
        } else {
            long hours = Math.round(min) / 60;
            long minutes = Math.round(min) - (60 * hours);
            return (hours + "h " + minutes + "min");
        }
    }


}
