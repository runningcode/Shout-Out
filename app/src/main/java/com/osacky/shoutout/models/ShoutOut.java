package com.osacky.shoutout.models;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.common.base.Joiner;

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
        updateShoutout(p);
    }

    public LatLng getLocation() {
        return new LatLng(lat, lon);
    }

    public void updateShoutout(MapPoint p) {
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
        Joiner joiner = Joiner.on(" ").useForNull("null");
        return joiner.join(id, "name is", name, "status is ", getStatus(), "lat is", lat,
                "lon is", lon);
    }

    @Override
    public int hashCode() {
        if (TextUtils.isEmpty(id)) {
            throw new IllegalStateException("Id should not be null or empty");
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
