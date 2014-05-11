package com.osacky.shoutout;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.base.Joiner;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;
import com.osacky.shoutout.models.MapPoint;
import com.osacky.shoutout.models.ShoutOut;
import com.osacky.shoutout.utils.LatLngInterpolator;
import com.osacky.shoutout.utils.MarkerAnimation;
import com.osacky.shoutout.views.ClosedPinView;
import com.osacky.shoutout.views.ClosedPinView_;
import com.osacky.shoutout.views.OpenPinView;
import com.osacky.shoutout.views.OpenPinView_;
import com.osacky.shoutout.views.SpringyImageView;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Touch;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.osacky.shoutout.utils.Constants.ARG_SECTION_NUMBER;
import static com.osacky.shoutout.utils.Constants.FASTEST_INTERVAL;
import static com.osacky.shoutout.utils.Constants.FIREBASE_URL;
import static com.osacky.shoutout.utils.Constants.LOC_PATH;
import static com.osacky.shoutout.utils.Constants.UPDATE_INTERVAL;

@EFragment(R.layout.fragment_map)
@OptionsMenu(R.menu.map)
public class MapHolderFragment extends Fragment
        implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener,
        LoggedInCallback, LocationListener {

    private static final String LOG_TAG = MapHolderFragment.class.getSimpleName();
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final Firebase mRef = new Firebase(FIREBASE_URL);
    private final Map<String, ShoutOut> mShoutCollection = new HashMap<>();
    private final Map<Marker, ShoutOut> mReverseLookup = new HashMap<>();
    private final Object mCollectionLock = new Object();

    private boolean mIsSavedInstanceState;
    private LocationRequest mLocationRequest;
    private ClosedPinView mClosedPinView;
    private OpenPinView mOpenPinView;
    private IconGenerator mClosedIconGenerator;
    private IconGenerator mOpenIconGenerator;
    private LocationClient mLocationClient;
    private SystemBarTintManager mSystemBarTintManager;
    private Marker mVisibleMarker = null;
    private ActionMode mActionMode;

    private SupportMapFragment mapFragment;

    @FragmentArg(ARG_SECTION_NUMBER)
    int sectionNumber;

    @ViewById(R.id.add_status_button)
    SpringyImageView addStatusButton;

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
        mSystemBarTintManager = new SystemBarTintManager(getActivity());

        mClosedIconGenerator = new IconGenerator(getActivity());
        mOpenIconGenerator = new IconGenerator(getActivity());

        mClosedPinView = ClosedPinView_.build(getActivity());
        mOpenPinView = OpenPinView_.build(getActivity());

        mClosedIconGenerator.setContentView(mClosedPinView);
        mOpenIconGenerator.setContentView(mOpenPinView);

        mClosedIconGenerator.setBackground(null);
        mOpenIconGenerator.setBackground(null);
    }

    @AfterViews
    void setUp() {
        final SystemBarTintManager.SystemBarConfig config = mSystemBarTintManager.getConfig();
        mSystemBarTintManager.setStatusBarTintEnabled(true);
        mSystemBarTintManager.setStatusBarTintColor(getResources().getColor(R.color.blue));
        mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        getMap().setOnMarkerClickListener(this);
        getMap().setOnMapClickListener(this);
        getMap().setOnCameraChangeListener(this);
        getMap().setMyLocationEnabled(true);
        getMap().setPadding(0, config.getPixelInsetTop(true),
                config.getPixelInsetRight(),
                config.getPixelInsetBottom());
        addStatusButton.setPadding(0, 0, 0, config.getPixelInsetBottom());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addStatusButton.setAnimSpring(1);
            }
        }, 1000);
    }

    @Override
    public void onStart() {
        super.onStart();
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

    @Override
    public void onConnected(Bundle bundle) {
        final Location location = mLocationClient.getLastLocation();
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
        if (!mIsSavedInstanceState) {
            try {
                final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location
                        .getLatitude(), location.getLongitude()), 14);
                getMap().animateCamera(cameraUpdate);
            } catch (NullPointerException ignored) {
            }
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

    @Override
    public void onLoggedIn() {
        mRef.child(LOC_PATH).addChildEventListener(locationChildListener);
    }

    @Background
    void processSnapShot(DataSnapshot snapshot) {
        final String key = snapshot.getName();
        Log.i(LOG_TAG, "key added/updated is " + key + " value is " + snapshot.getValue());
        final MapPoint point = snapshot.getValue(MapPoint.class);
        boolean isStatus = false;

        ShoutOut shoutOut;
        LatLng oldPos = null;
        synchronized (mCollectionLock) {
            if (mShoutCollection.containsKey(key)) {
                shoutOut = mShoutCollection.get(key);
                oldPos = shoutOut.getLocation();
                boolean empty = TextUtils.isEmpty(point.getStatus());
                Log.i(LOG_TAG, "is empty " + empty + " shoutout " + shoutOut.getStatus() +
                        " point " + point.getStatus());
                isStatus = !TextUtils.isEmpty(point.getStatus()) && !point.getStatus().equals
                        (shoutOut
                                .getStatus());
                shoutOut.updateShoutout(point);
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
                final String imageURL = user.getString("picURL");
                shoutOut.setName(user.getUsername());
                final Bitmap bitmap = BitmapFactory.decodeStream(new URL(imageURL).openStream());
                shoutOut.setBitmap(bitmap);
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        }
        addToMap(shoutOut, oldPos, isStatus);
    }

    @UiThread
    void addToMap(ShoutOut shoutOut, LatLng oldPos, boolean isStatus) {
        Log.i(LOG_TAG, "is status " + isStatus);
        if (shoutOut.getMarker() == null) {
            mClosedPinView.bind(shoutOut.getBitmap());
            final Bitmap icon = mClosedIconGenerator.makeIcon();
            MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon))
                    .position(shoutOut.getLocation())
                    .anchor(0, 1);
            if (mapFragment.getMap() != null) {
                final Marker marker = mapFragment.getMap().addMarker(markerOptions);
                mReverseLookup.put(marker, shoutOut);
                shoutOut.setMarker(marker);
            }
        } else {
            if (isStatus) {
                Log.i(LOG_TAG, "displaying open pin");
                mOpenPinView.bind(shoutOut);
                final Bitmap icon = mOpenIconGenerator.makeIcon();
                shoutOut.getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(icon));
            }
            LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Spherical();
            MarkerAnimation.animateMarker(shoutOut.getMarker(), oldPos, latLngInterpolator);
        }
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
        Log.i(LOG_TAG, "updating my location");
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
    public void onCameraChange(CameraPosition cameraPosition) {
        final LatLng target = cameraPosition.target;
        double distance = Double.MAX_VALUE;
        ShoutOut closest = null;
        for (ShoutOut shoutOut : mShoutCollection.values()) {
            final double v = SphericalUtil.computeDistanceBetween(target, shoutOut.getLocation());
            if (v < distance) {
                distance = v;
                closest = shoutOut;
            }
        }
        if (closest != null && closest.getMarker() != null) {
            closest.getMarker().showInfoWindow();
        }
    }

    GoogleMap getMap() {
        if (mapFragment == null) {
            return null;
        }
        return mapFragment.getMap();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        handleMarkerClick(marker);
        return false;
    }

    private void handleMarkerClick(Marker marker) {
        if (marker == mVisibleMarker) {
            closeMarker(mVisibleMarker);
        } else {
            Log.i(LOG_TAG, "opening another marker");
            if (mVisibleMarker != null) {
                Log.i(LOG_TAG, "closing old marker");
                closeMarker(marker);
            }
            final ShoutOut shoutOut = mReverseLookup.get(marker);
            mActionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(new
                    ActionCallback(shoutOut));
            mOpenPinView.bind(shoutOut);
            final Bitmap icon = mOpenIconGenerator.makeIcon();
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
            mVisibleMarker = marker;
        }
    }

    private void closeMarker(Marker marker) {
        final ShoutOut toClose = mReverseLookup.get(mVisibleMarker);
        mClosedPinView.bind(toClose.getBitmap());
        final Bitmap bitmap = mClosedIconGenerator.makeIcon();
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        mVisibleMarker = null;
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Touch(R.id.add_status_button)
    void touchButtion(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                addStatusButton.setImageResource(R.drawable.ic_compose_status_selected);
                addStatusButton.setTouchValue(1);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                addStatusButton.setImageResource(R.drawable.ic_compose_status_normal);
                addStatusButton.setTouchValue(0);
                DialogFragment dialogFragment = new PostStatusFragment();
                dialogFragment.show(getFragmentManager(), "STATUS");
                break;
        }
    }

    private class ActionCallback implements ActionMode.Callback {
        final ShoutOut selectedShout;

        ActionCallback(ShoutOut shoutOut) {
            selectedShout = shoutOut;
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            mSystemBarTintManager.setStatusBarTintColor(getResources().getColor(android.R.color.white));
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.action_selected_shout, menu);
            MenuItem item = menu.findItem(R.id.action_share);

            ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            Joiner joiner = Joiner.on(" ").skipNulls();
            shareIntent.putExtra(Intent.EXTRA_TEXT, joiner.join(selectedShout.getName(),
                    "shared", selectedShout.getStatus(), "using Shoutout!"));
            shareIntent.setType("text/plain");
            shareActionProvider.setShareIntent(shareIntent);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_favorite:
                    Toast.makeText(getActivity(), "This is not implemented yet",
                            Toast.LENGTH_SHORT).show();
                    mActionMode.finish();
                    return true;
                case R.id.action_add_friend:
                    Toast.makeText(getActivity(), "This is not implemented yet",
                            Toast.LENGTH_SHORT).show();
                    mActionMode.finish();
                    return true;
                case R.id.action_share:
                    Toast.makeText(getActivity(), "SHARE!!",
                            Toast.LENGTH_SHORT).show();
                    mActionMode.finish();
                    return false;

            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mSystemBarTintManager.setStatusBarTintColor(getResources().getColor(R.color.blue));
            mActionMode = null;
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mVisibleMarker != null) {
            handleMarkerClick(mVisibleMarker);
        }
    }
}
