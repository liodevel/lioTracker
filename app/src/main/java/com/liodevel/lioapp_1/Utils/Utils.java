package com.liodevel.lioapp_1.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by emilio on 29/12/15.
 */
public class Utils {

    public static String MONDAY = "Monday";
    public static String TUESDAY = "Tuesday";
    public static String WEDNESDAY = "Wednesday";
    public static String THURSDAY = "Thursday";
    public static String FRIDAY = "Friday";
    public static String SATURDAY = "Saturday";
    public static String SUNDAY = "Sunday";


    /**
     * Shows a Toast
     * @param context
     * @param message
     */
    public static void showMessage(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
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
