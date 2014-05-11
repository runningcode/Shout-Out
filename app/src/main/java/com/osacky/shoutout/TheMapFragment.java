package com.osacky.shoutout;

import android.app.Activity;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.osacky.shoutout.models.Point;
import com.osacky.shoutout.models.ShoutOut;
import com.osacky.shoutout.utils.LatLngInterpolator;
import com.osacky.shoutout.utils.MarkerAnimation;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.osacky.shoutout.utils.Constants.ARG_SECTION_NUMBER;
import static com.osacky.shoutout.utils.Constants.FASTEST_INTERVAL;
import static com.osacky.shoutout.utils.Constants.FIREBASE_URL;
import static com.osacky.shoutout.utils.Constants.LOC_PATH;
import static com.osacky.shoutout.utils.Constants.UPDATE_INTERVAL;

@EFragment
public class TheMapFragment extends SupportMapFragment
        implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LoggedInCallback, LocationListener {

    private static final String LOG_TAG = TheMapFragment.class.getSimpleName();
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final Firebase mRef = new Firebase(FIREBASE_URL);
    private boolean mIsSavedInstanceState;
    private LocationRequest mLocationRequest;
    private Map<String, ShoutOut> mShoutCollection = new HashMap<>();
    private final Object collectionLock = new Object();
    private ImageView mImageView;
    private IconGenerator mIconGenerator;
    private LocationClient mLocationClient;

    @FragmentArg(ARG_SECTION_NUMBER)
    int sectionNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (savedInstanceState != null) {
            mIsSavedInstanceState = true;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mLocationClient = new LocationClient(getActivity(), this, this);

        mIconGenerator = new IconGenerator(getActivity());
        mImageView = new ImageView(getActivity());

        int padding = (int) getResources().getDimension(R.dimen.profile_padding);
        int mProfDimension = (int) getResources().getDimension(R.dimen.profile_image_size);
        mImageView.setLayoutParams(new ViewGroup.LayoutParams(mProfDimension, mProfDimension));
        mImageView.setPadding(padding, padding, padding, padding);
        mIconGenerator.setContentView(mImageView);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).onSectionAttached(sectionNumber);
        mLocationClient.connect();
    }

    @Override
    public void onStop() {
        if (mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(this);
        }
        mLocationClient.disconnect();
        super.onStop();
    }

    @AfterViews
    void setUpView() {
        getMap().setMyLocationEnabled(true);
        getMap().getUiSettings().setZoomControlsEnabled(false);
    }

    @Override
    public void onConnected(Bundle bundle) {
        final Location location = mLocationClient.getLastLocation();
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
        if (!mIsSavedInstanceState) {
            final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location
                    .getLatitude(), location.getLongitude()), 14);
            getMap().animateCamera(cameraUpdate);
        }
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(getActivity(), "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        }
    }

    @Background
    void processSnapShot(DataSnapshot snapshot) {
        final String key = snapshot.getName();
        Log.i(LOG_TAG, "key added " + key);
        final Point point = snapshot.getValue(Point.class);
        ShoutOut shoutOut;
        LatLng oldPos = null;
        synchronized (collectionLock) {
            if (mShoutCollection.containsKey(key)) {
                shoutOut = mShoutCollection.get(key);
                oldPos = shoutOut.getLocation();
                shoutOut.setLocation(point);
            } else {
                shoutOut = new ShoutOut(key, point);
            }
            mShoutCollection.put(key, shoutOut);
        }
        if (shoutOut.getBitmap() == null) {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("objectId", shoutOut.getId());
            try {
                final ParseUser user = query.getFirst();
                Log.i(LOG_TAG, user.getUsername());
                final String imageURL = user.getString("picURL");
                final Bitmap bitmap = BitmapFactory.decodeStream(new URL(imageURL).openStream());
                shoutOut.setBitmap(bitmap);
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        }
        addToMap(shoutOut, oldPos);
    }

    @UiThread
    void addToMap(ShoutOut shoutOut, LatLng oldPos) {
        if (shoutOut.getMarker() == null) {
            mImageView.setImageBitmap(shoutOut.getBitmap());
            Bitmap icon = mIconGenerator.makeIcon();
            MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon))
                    .position(shoutOut.getLocation());
            shoutOut.setMarker(getMap().addMarker(markerOptions));
        } else {
            LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Spherical();
            MarkerAnimation.animateMarker(shoutOut.getMarker(), oldPos, latLngInterpolator);
        }

    }

    @Override
    public void onLoggedIn() {
        mRef.child(LOC_PATH).addChildEventListener(locationChildListener);
    }

    ChildEventListener locationChildListener = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            processSnapShot(dataSnapshot);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            processSnapShot(dataSnapshot);
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
    };

    @Override
    public void onLocationChanged(Location location) {
        updateServerLocation(location);
    }

    private void updateServerLocation(Location location) {
        Log.i(LOG_TAG, "updating location");
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser != null) {
            final Map<String, Object> locationUpdate = new HashMap<>();
            locationUpdate.put("lat", location.getLatitude());
            locationUpdate.put("lon", location.getLongitude());
            mRef.child(LOC_PATH).child(parseUser.getObjectId()).updateChildren(locationUpdate);
            ParseGeoPoint parseGeoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            parseUser.put("geo", parseGeoPoint);
            parseUser.saveInBackground();
        }
    }
}
