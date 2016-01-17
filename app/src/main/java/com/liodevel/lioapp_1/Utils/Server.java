package com.liodevel.lioapp_1.Utils;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.liodevel.lioapp_1.Objects.Track;
import com.liodevel.lioapp_1.Objects.TrackPoint;
import com.liodevel.lioapp_1.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
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
        Utils.logInfo("getTracksByUser()");

        ArrayList<Track> ret = new ArrayList<>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("track");
        ParseUser user = ParseUser.getCurrentUser();
        query.whereEqualTo("user", user);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, com.parse.ParseException e) {
                if (e == null) {
                    for (ParseObject parseObject : objects) {
                        Track track = new Track();
                        track.setObjectId((String) parseObject.get("objectId"));
                        track.setDate((Date) parseObject.get("date"));
                        track.setDateEnd((Date) parseObject.get("dateEnd"));
                        track.setClosed(parseObject.getBoolean("closed"));
                        Utils.logInfo("Track: " + track.getDate());
                        //ret.add(track);
                    }
                } else {
                    // Something went wrong.
                    Utils.logError(e.toString());
                }
            }
        });

        return ret;

    }

    /**
     * Borrar Track
     * @param objectId
     */
    public static void deleteTrackByObjectId(String objectId) {
        Utils.logInfo("deleteTrackByObjectId()");
        ParseObject trackObject = null;

        ParseQuery<ParseObject> queryTrackObject = ParseQuery.getQuery("track");
        queryTrackObject.whereEqualTo("objectId", objectId);
        try {
            List<ParseObject> parseQueriesTrackObject = queryTrackObject.find();
            trackObject = parseQueriesTrackObject.get(0);
            trackObject.delete();
            trackObject.saveInBackground();
            Utils.logInfo("Track ID: " + trackObject.getObjectId());

        } catch (ParseException e) {
            Utils.logInfo("Error deleting: " + e.toString());
        }
    }





    /**
     * Borra los Tracks seleccionados
     */
    public static void deleteSelectedTracks(ArrayList<String> selectedTracksId, Context context) {
        Utils.logInfo("deleteSelectedTracks()");
        if (selectedTracksId.size() > 0) {
            ParseObject trackObject = null;
            ParseQuery<ParseObject> queryTrackObject = ParseQuery.getQuery("track");
            try {
                int cont = 0;
                for (String id : selectedTracksId) {
                    queryTrackObject.whereEqualTo("objectId", id);
                    List<ParseObject> parseQueriesTrackObject = queryTrackObject.find();
                    Utils.logInfo("Find: " + parseQueriesTrackObject.toString());
                    trackObject = parseQueriesTrackObject.get(0);
                    trackObject.delete();
                    //trackObject.save();
                    Utils.logInfo("Track ID: " + trackObject.getObjectId());
                    cont++;
                }
                selectedTracksId = new ArrayList<>();
                Utils.showMessage(context, cont + " " + context.getResources().getString(R.string.tracks_deleted));
            } catch (ParseException e) {
                Utils.logInfo("Error deleting: " + e.toString());
                Utils.showMessage(context, context.getResources().getString(R.string.error_deleting_tracks));
            }
        }
    }



    /**
     *
     * @param objectId Track objectId
     */
    public static void fixTrack(String objectId){

        ParseObject trackObject = null;
        Track currentTrack = new Track();

        boolean ret = false;
        LatLng prevPos = null;
        LatLng actualPos = null;
        TrackPoint previousTrackPoint = new TrackPoint();

        ParseQuery<ParseObject> queryTrackObject = ParseQuery.getQuery("track");
        queryTrackObject.whereEqualTo("objectId", objectId);

        try {
            List<ParseObject> parseQueriesTrackObject = queryTrackObject.find();
            trackObject = parseQueriesTrackObject.get(0);
            currentTrack.setObjectId(parseQueriesTrackObject.get(0).getObjectId());
            currentTrack.setDate((Date) parseQueriesTrackObject.get(0).get("date"));
            currentTrack.setDateEnd((Date) parseQueriesTrackObject.get(0).get("dateEnd"));
            currentTrack.setDistance((float) parseQueriesTrackObject.get(0).getDouble("distance"));
            currentTrack.setInfo((String) parseQueriesTrackObject.get(0).get("info"));
            currentTrack.setFavorite(parseQueriesTrackObject.get(0).getBoolean("favorite"));
            Utils.logInfo("Track ID: " + trackObject.getObjectId());
            ret = true;
        } catch (ParseException e) {
            Utils.logInfo("Error: " + e.toString());
            ret = false;
        }

        if (trackObject != null) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("trackPoint");
            query.whereEqualTo("track", trackObject);
            query.setLimit(1000);
            query.orderByAscending("date");
            int cont = 0;
            try {
                List<ParseObject> parseQueries = query.find();
                float totalDistance = 0;
                Date lastDate = null;
                for (ParseObject parseObject : parseQueries) {
                    cont++;
                    TrackPoint trackPoint = new TrackPoint();
                    trackPoint.setObjectId(parseObject.getObjectId());
                    trackPoint.setDate((Date) parseObject.get("date"));
                    lastDate = (Date) parseObject.get("date");
                    trackPoint.setPosition((ParseGeoPoint) parseObject.get("position"));
                    actualPos = new LatLng(trackPoint.getPosition().getLatitude(), trackPoint.getPosition().getLongitude());
                    if (prevPos != null) {
                        if(previousTrackPoint != null) {

                            Location selected_location=new Location("locationA");
                            selected_location.setLatitude(trackPoint.getPosition().getLatitude());
                            selected_location.setLongitude( trackPoint.getPosition().getLongitude());
                            Location near_locations=new Location("locationA");
                            near_locations.setLatitude(previousTrackPoint.getPosition().getLatitude());
                            near_locations.setLongitude(previousTrackPoint.getPosition().getLongitude());

                            double distance = selected_location.distanceTo(near_locations);
                            totalDistance = totalDistance + ((float)distance);
                        }
                    }

                    prevPos = actualPos;
                    previousTrackPoint = trackPoint;
                }
                currentTrack.setDistance(totalDistance);
                trackObject.put("distance", totalDistance);
                trackObject.put("closed", true);

                if (lastDate != null){
                    trackObject.put("dateEnd", lastDate);
                }
                trackObject.save();
                Utils.logInfo("Track Fixed! ");
                ret = true;
            } catch (ParseException e) {
                Utils.logInfo("Error: " + e.toString());
                ret = false;
            }
        }


    }


}
