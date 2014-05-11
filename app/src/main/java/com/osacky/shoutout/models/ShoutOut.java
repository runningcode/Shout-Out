package com.osacky.shoutout.models;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class ShoutOut {
    private double lat;
    private double lon;
    private final String id;
    private String status;
    private Bitmap bitmap;
    private Marker marker;

    public ShoutOut(String id, Point p) {
        this.id = id;
        lat = p.getLat();
        lon = p.getLon();
    }

    public LatLng getLocation() {
        return new LatLng(lat, lon);
    }

    public void setLocation(Point p) {
        lat = p.getLat();
        lon = p.getLon();
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    @Override
    public int hashCode() {
        if (TextUtils.isEmpty(id)) {
            throw new IllegalStateException("Id should not be 0");
        }
        return id.hashCode();
    }
}
