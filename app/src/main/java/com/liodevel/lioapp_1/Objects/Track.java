package com.liodevel.lioapp_1.Objects;

import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by emilio on 19/12/2015.
 */
public class Track {

    private String objectId;
    private ParseUser user;
    private Date date;
    private Date dateEnd;
    private float distance;         // en metros
    private long duration;          // en segundos
    private int vehicle;            // 1-Coche; 2-Moto; 3-Bici; 4-Patinete; 5-Andando
    private String info;
    private ParseGeoPoint fromLocation;
    private ParseGeoPoint toLocation;
    private boolean favorite;

    private float averageSpeed;
    private float topSpeed;

    private boolean isChecked;

    private ArrayList<TrackPoint> trackPoints;

    public Track() {
        objectId = "";
        user = new ParseUser();
        date = new Date();
        dateEnd = new Date();
        distance = 0;
        fromLocation = new ParseGeoPoint();
        toLocation = new ParseGeoPoint();
        favorite = false;
        vehicle = 1;

        isChecked = false;
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

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public float getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(float averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public float getTopSpeed() {
        return topSpeed;
    }

    public void setTopSpeed(float topSpeed) {
        this.topSpeed = topSpeed;
    }
}
