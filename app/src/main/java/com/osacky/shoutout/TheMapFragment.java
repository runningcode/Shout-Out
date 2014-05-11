package com.osacky.shoutout;

import android.app.Activity;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.osacky.shoutout.models.ShoutOut;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.osacky.shoutout.Constants.ARG_SECTION_NUMBER;
import static com.osacky.shoutout.Constants.FASTEST_INTERVAL;
import static com.osacky.shoutout.Constants.FIREBASE_URL;
import static com.osacky.shoutout.Constants.LOC_PATH;
import static com.osacky.shoutout.Constants.UPDATE_INTERVAL;

@EFragment
public class TheMapFragment extends SupportMapFragment
        implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LoggedInCallback, LocationListener,
        ClusterManager.OnClusterItemClickListener<ShoutOut>,
        ClusterManager.OnClusterItemInfoWindowClickListener<ShoutOut>,
        ClusterManager.OnClusterClickListener<ShoutOut>,
        ClusterManager.OnClusterInfoWindowClickListener<ShoutOut> {

    private static final String LOG_TAG = TheMapFragment.class.getSimpleName();
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final Firebase mRef = new Firebase(FIREBASE_URL);
    private boolean mIsSavedInstanceState;
    private LocationRequest mLocationRequest;
    private ClusterManager<ShoutOut> mClusterManager;
    private Map<String, ShoutOut> mShoutCollection = new HashMap<>();
    private final Object collectionLock = new Object();

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
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mClusterManager = new ClusterManager<>(getActivity(), getMap());
        mClusterManager.setRenderer(new ShoutOutRenderer());
        getMap().setOnCameraChangeListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
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
        String key = snapshot.getName();
        Log.i(LOG_TAG, "key added " + key);
        ShoutOut shout = snapshot.getValue(ShoutOut.class);
        boolean isUpdate = false;
        synchronized (collectionLock) {
            if (mShoutCollection.containsKey(key)) {
                isUpdate = true;
                ShoutOut old = mShoutCollection.get(key);
                old.setLon(shout.getLon());
                old.setLat(shout.getLat());
                shout = old;
            }
            mShoutCollection.put(key, shout);
        }
        shout.setId(key);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", shout.getId());
        try {
            final ParseUser user = query.getFirst();
            Log.i(LOG_TAG, user.getUsername());
            final String imageURL = user.getString("picURL");
            final Bitmap bitmap = BitmapFactory.decodeStream(new URL(imageURL).openStream());
            shout.setBitmap(bitmap);

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        reCluster(shout, isUpdate);
    }

    @UiThread
    void reCluster(ShoutOut shoutOut, boolean isUpdate) {
        if (!isUpdate) {
            mClusterManager.addItem(shoutOut);
        }
        mClusterManager.cluster();
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

    @Override
    public boolean onClusterItemClick(ShoutOut shoutOut) {
        return false;
    }

    @Override
    public boolean onClusterClick(Cluster<ShoutOut> shoutOutCluster) {
        return false;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<ShoutOut> shoutOutCluster) {

    }

    @Override
    public void onClusterItemInfoWindowClick(ShoutOut shoutOut) {

    }

    private class ShoutOutRenderer extends DefaultClusterRenderer<ShoutOut> {

        private final ImageView mImageView;
        private final IconGenerator mIconGenerator = new IconGenerator(getActivity());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getActivity());
        private final ImageView mClusterImageView;
        private final int mProfDimension;


        public ShoutOutRenderer() {
            super(getActivity(), getMap(), mClusterManager);

            View multiProfile = getActivity().getLayoutInflater().inflate(R.layout.multi_profile,
                    null);
            int padding = (int) getResources().getDimension(R.dimen.profile_padding);
            mProfDimension = (int) getResources().getDimension(R.dimen.profile_image_size);

            mClusterIconGenerator.setContentView(multiProfile);
            assert multiProfile != null;
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getActivity());
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mProfDimension, mProfDimension));
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(ShoutOut item, final MarkerOptions markerOptions) {
            mImageView.setImageBitmap(item.getBitmap());
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<ShoutOut> cluster, MarkerOptions markerOptions) {
            List<Drawable> profilePhotos = new ArrayList<>(Math.min(4, cluster.getSize()));
            int width = mProfDimension;
            int height = mProfDimension;

            for (ShoutOut shoutOut : cluster.getItems()) {
                if (profilePhotos.size() == 4) break;
                Drawable drawable = new BitmapDrawable(getResources(), shoutOut.getBitmap());
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }

            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);
            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }
}
