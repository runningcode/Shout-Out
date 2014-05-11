package com.osacky.shoutout.models;

import android.location.Location;

public class Point {

    final double lat;
    final double lon;

    public Point(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public Point(Location location) {
        lat = location.getLatitude();
        lon = location.getLongitude();
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
