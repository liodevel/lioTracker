package com.liodevel.lioapp_1.Utils;

import android.util.Log;

import com.liodevel.lioapp_1.Objects.Track;
import com.liodevel.lioapp_1.Objects.TrackPoint;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        dataObject.put("accuracy", tp.getAccuracy());
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

    public static ArrayList<Track> getTracksByCurrentUser(){
        Log.i("LIOTRACK", "getTracksByUser()");

        ArrayList<Track> ret = new ArrayList<>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("track");
        ParseUser user = ParseUser.getCurrentUser();
        query.whereEqualTo("user", user);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, com.parse.ParseException e) {
                if (e == null) {
                    for (ParseObject parseObject : objects){
                        Track track = new Track();
                        track.setObjectId((String) parseObject.get("objectId"));
                        track.setDate((Date) parseObject.get("date"));
                        track.setDateEnd((Date) parseObject.get("dateEnd"));
                        Log.i("LIOTRACK", "Track: " + track.getDate());
                        //ret.add(track);
                    }
                } else {
                    // Something went wrong.
                    Log.i("LIOTRACK", "Error: " + e.toString());
                }
            }
        });

        return ret;

    }


}
