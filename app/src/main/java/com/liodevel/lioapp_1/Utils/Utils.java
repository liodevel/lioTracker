package com.liodevel.lioapp_1.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.parse.ParseUser;

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
    public static final String DKGREEN = "ff006e2e";
    public static final String LTGRAY = "ffcccccc";
    public static final String MAGENTA = "ffff00ff";
    public static final String RED = "ffff0000";
    public static final String TRANSPARENT = "00000000";
    public static final String WHITE = "ffffffff";
    public static final String YELLOW = "ffffff00";
    public static final String ORANGE = "ffe55300";


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


    /**
     * Escribe info en el log
     * @param message
     */
    public static void logInfo(String message){
        Log.i("myTracker", message);
    }

    /**
     * Escribe error en el log
     * @param message
     */
    public static void logError(String message){
        Log.e("myTracker", "ERROR: " + message);
    }


    /**
     * Convierte minutos a formato HH:mm
     * @param min
     * @return
     */
    public static String minutesToHour(double min){
        if (min < 60){
            return (Math.round(min) + " min");
        } else {
            long hours = Math.round(min) / 60;
            long minutes = Math.round(min) - (60 * hours);
            return (hours + "h " + minutes + " min");
        }
    }

    /**
     * Convierte minutos a formato HH:mm
     * @param seconds
     * @return
     */
    public static String secondsToHour(long seconds){
        String zeroSeconds = "";
        if (seconds < 60){
            return (seconds + " sec");
        } else {
            long minutes = seconds / 60;
            long secondsRet = seconds - (60 * minutes);
            if (secondsRet < 10){
                zeroSeconds = "0";
            }
            return (minutes + ":" + zeroSeconds + secondsRet);
        }
    }


    /**
     * Comprueba el estado de la conexión
     * @param context
     * @return
     */
    public static boolean checkConn(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }



}
