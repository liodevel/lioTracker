package com.liodevel.lioapp_1.Utils;

import android.content.Context;
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



}
