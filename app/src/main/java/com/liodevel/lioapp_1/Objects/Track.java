package com.liodevel.lioapp_1.Objects;

import android.location.Location;

import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by emilio on 19/12/2015.
 */
public class Track {

    String objectId;
    ParseUser user;
    Date date;
    Date dateEnd;
    float distance;      // en metros
    long duration;      // en segundos
    int vehicle;        // 1-Coche; 2-Transporte público; 3-Bici; 4-Andando;
    String info;
    ParseGeoPoint fromLocation;
    ParseGeoPoint toLocation;

    ArrayList<TrackPoint> trackPoints;

    public Track() {
        objectId = "";
        user = new ParseUser();
        date = new Date();
        dateEnd = new Date();
        distance = 0;
        fromLocation = new ParseGeoPoint();
        toLocation = new ParseGeoPoint();

        trackPoints = new ArrayList<>();
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public ParseUser getUser() {
        return user;
    }

    public void setUser(ParseUser user) {
        this.user = user;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public ArrayList<TrackPoint> getTrackPoints() {
        return trackPoints;
    }

    public void setTrackPoints(ArrayList<TrackPoint> trackPoints) {
        this.trackPoints = trackPoints;
    }

    public int getVehicle() {
        return vehicle;
    }

    public void setVehicle(int vehicle) {
        this.vehicle = vehicle;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public ParseGeoPoint getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(ParseGeoPoint fromLocation) {
        this.fromLocation = fromLocation;
    }

    public ParseGeoPoint getToLocation() {
        return toLocation;
    }

    public void setToLocation(ParseGeoPoint toLocation) {
        this.toLocation = toLocation;
    }
}
