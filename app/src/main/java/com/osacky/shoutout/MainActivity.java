package com.osacky.shoutout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.firebase.client.Firebase;
import com.firebase.simplelogin.SimpleLogin;
import com.firebase.simplelogin.SimpleLoginAuthenticatedHandler;
import com.firebase.simplelogin.User;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;

import static com.osacky.shoutout.Constants.FIREBASE_URL;

@EActivity(R.layout.activity_main)
public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        Session.StatusCallback {

    @SuppressWarnings("unused")
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    @FragmentById(R.id.navigation_drawer)
    NavigationDrawerFragment mNavigationDrawerFragment;
    private UiLifecycleHelper mUiLifecycleHelper;
    private SimpleLogin mAuthClient;
    private Session mSession;
    private Firebase ref;
    private GraphUser mUser;
    private LoggedInCallback mLoggedInCallback;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mUiLifecycleHelper = new UiLifecycleHelper(this, this);
        mUiLifecycleHelper.onCreate(savedInstanceState);
        ref = new Firebase(FIREBASE_URL);
        mAuthClient = new SimpleLogin(ref, this);
    }

    private void handleNewSession(Session session, SessionState state, Exception e) {
        Log.i(LOG_TAG, "new session");
        mSession = session;
        mAuthClient.loginWithFacebook(getString(R.string.fb_app_id), session.getAccessToken(),
                new SimpleLoginAuthenticatedHandler() {
                    @Override
                    public void authenticated(com.firebase.simplelogin.enums.Error error, User user) {
                        if (error == null) {
                            Request.newMeRequest(mSession, new Request.GraphUserCallback() {
                                        // callback after Graph API response with user object
                                        @Override
                                        public void onCompleted(GraphUser user, Response response) {
                                            if (user != null) {
                                                mUser = user;
                                                mLoggedInCallback.onLoggedIn();
                                                Toast.makeText(MainActivity.this,
                                                        "Welcome " + user.getName(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                            ).executeAsync();
                        } else {
                            // TODO handle error
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mUiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUiLifecycleHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUiLifecycleHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUiLifecycleHelper.onDestroy();
    }

    @AfterViews
    void setUpView() {
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position) {
            case 0:
                final TheMapFragment mapFrag = TheMapFragment_.builder()
                        .sectionNumber(position + 1)
                        .build();
                mLoggedInCallback = mapFrag;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, mapFrag)
                                .commit();
                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment_.builder()
                                .sectionNumber(position + 1)
                                .build())
                        .commit();
                break;
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment_.builder()
                                .sectionNumber(position + 1)
                                .build())
                        .commit();
                break;
            default:
                throw new IllegalArgumentException("Don't have a fragment for position " +
                        position);
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mUiLifecycleHelper.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void call(Session session, SessionState sessionState, Exception e) {
        handleNewSession(session, sessionState, e);
    }
}
