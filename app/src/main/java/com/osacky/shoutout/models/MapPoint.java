package com.osacky.shoutout.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MapPoint {

    String status;
    double lat;
    double lon;

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getStatus() {
        return status;
    }
}
