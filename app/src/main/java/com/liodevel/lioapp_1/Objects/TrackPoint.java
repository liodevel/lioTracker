package com.liodevel.lioapp_1.Objects;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by emilio on 19/12/2015.
 */
public class TrackPoint {

    private String objectId;
    private ParseObject track;
    private ParseGeoPoint position;
    private Date date;
    private float accuracy;
    private String provider;

    private double speed;

    public TrackPoint() {
        objectId = "";
        position = new ParseGeoPoint();
        date = new Date();
        speed = 0.0f;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public ParseGeoPoint getPosition() {
        return position;
    }

    public void setPosition(ParseGeoPoint position) {
        this.position = position;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ParseObject getTrack() {
        return track;
    }

    public void setTrack(ParseObject track) {
        this.track = track;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
