package com.liodevel.lioapp_1.Objects;

import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by emilio on 19/12/2015.
 */
public class TrackPoint {

    String objectId;
    ParseGeoPoint position;
    Date date;

    public TrackPoint() {
        objectId = "";
        position = new ParseGeoPoint();
        date = new Date();
    }

}
