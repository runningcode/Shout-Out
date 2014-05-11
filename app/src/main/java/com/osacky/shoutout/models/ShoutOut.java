package com.osacky.shoutout.models;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class ShoutOut {
    private double lat;
    private double lon;
    private final String id;
    private String name;
    private String status;
    private Bitmap bitmap;
    private Marker marker;

    public ShoutOut(String id, MapPoint p) {
        this.id = id;
        lat = p.getLat();
        lon = p.getLon();
    }

    public LatLng getLocation() {
        return new LatLng(lat, lon);
    }

    public void setLocation(MapPoint p) {
        if (p.getLat() != 0) {
            lat = p.getLat();
        }
        if (p.getLon() != 0) {
            lon = p.getLon();
        }
        if (!TextUtils.isEmpty(p.getStatus())) {
            status = p.getStatus();
        }
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
