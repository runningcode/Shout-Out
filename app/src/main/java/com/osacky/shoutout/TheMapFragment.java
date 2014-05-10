package com.osacky.shoutout;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.osacky.shoutout.models.Point;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import static com.osacky.shoutout.Constants.*;

@EFragment
public class TheMapFragment extends SupportMapFragment
        implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LoggedInCallback, ChildEventListener {

    private static final String LOG_TAG = TheMapFragment.class.getSimpleName();

    private LocationClient mLocationClient;
    private Firebase ref;

    @FragmentArg(ARG_SECTION_NUMBER)
    int sectionNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ref = new Firebase(Constants.FIREBASE_URL);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mLocationClient = new LocationClient(getActivity(), this, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).onSectionAttached(sectionNumber);
    }


    @AfterViews
    void setUpView() {
        getMap().setMyLocationEnabled(true);
        getMap().getUiSettings().setZoomControlsEnabled(false);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location lastLocation = mLocationClient.getLastLocation();
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation
                .getLatitude(), lastLocation.getLongitude()), 14);
        getMap().animateCamera(cameraUpdate);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLoggedIn() {
        ref.addChildEventListener(this);

    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        String name = dataSnapshot.getName();
        Log.i(LOG_TAG, name);
        final Object value = dataSnapshot.getValue();
        if (value != null) {
            Point p = dataSnapshot.getValue(Point.class);
            Log.i(LOG_TAG, "Lat is " + p.getLat());
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}
