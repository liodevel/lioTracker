package com.liodevel.lioapp_1.Objects;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by emilio on 19/12/2015.
 */
public class TrackPoint {

    String objectId;
    ParseObject track;
    ParseGeoPoint position;
    Date date;

    public TrackPoint() {
        objectId = "";
        position = new ParseGeoPoint();
        date = new Date();
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
}
