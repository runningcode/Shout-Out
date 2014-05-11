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

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;

@EActivity(R.layout.activity_main)
public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    @SuppressWarnings("unused")
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    @FragmentById(R.id.navigation_drawer)
    NavigationDrawerFragment mNavigationDrawerFragment;
    private LoggedInCallback mLoggedInCallback;
    private boolean loggedIn = false;
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
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            ParseFacebookUtils.logIn(this, new LogInCallback() {
                @Override
                public void done(final ParseUser parseUser, ParseException e) {
                    if (parseUser == null) {
                        Log.i(LOG_TAG, "cancelled");
                    } else {
                        Session session = ParseFacebookUtils.getSession();
                        Request.newMeRequest(session, new Request.GraphUserCallback() {
                            @Override
                            public void onCompleted(GraphUser graphUser, Response response) {
                                if (graphUser != null) {
                                    parseUser.put("picURL",
                                            "https://graph.facebook" +
                                                    ".com/" + graphUser
                                                    .getId() + "/picture?width=200&height=200"
                                    );
                                    parseUser.put("status", "Hello world!");
                                    parseUser.put("visible", true);
                                    parseUser.saveInBackground();
                                    mLoggedInCallback.onLoggedIn();
                                }
                            }
                        }).executeAsync();
                    }
                }
            });
        } else {
            loggedIn = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    @AfterViews
    void setUpView() {
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        if (loggedIn && mLoggedInCallback != null) {
            mLoggedInCallback.onLoggedIn();
        }
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
//        mUiLifecycleHelper.onSaveInstanceState(outState);
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
}
