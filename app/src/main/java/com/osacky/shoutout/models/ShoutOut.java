package com.osacky.shoutout.models;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ShoutOut implements ClusterItem {
    double lat;
    double lon;
    String id;
    String status;
    Bitmap bitmap;
    String url;

    public void setId(String id) {
        this.id = id;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getId() {
        return id;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
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

    @Override
    public LatLng getPosition() {
        return new LatLng(lat, lon);
    }
}
