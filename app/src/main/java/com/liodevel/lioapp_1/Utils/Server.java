package com.liodevel.lioapp_1.Utils;

import android.util.Log;

import com.liodevel.lioapp_1.Objects.Track;
import com.liodevel.lioapp_1.Objects.TrackPoint;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;

/**
 * Created by emilio on 21/12/2015.
 */
public class Server {

    /**
     * Envia un TrackPoint al Servidor
     * @param tp
     */
    public static void sendTrackPoint(TrackPoint tp){

        ParseObject dataObject = new ParseObject("trackPoint");
        dataObject.put("position", tp.getPosition());
        dataObject.put("date", tp.getDate());
        dataObject.put("track", tp.getTrack());
        dataObject.saveInBackground(new SaveCallback() {
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
