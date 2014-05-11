package com.osacky.shoutout;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

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
public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {

    @SuppressWarnings("unused")
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @FragmentById(R.id.map_holder)
    MapHolderFragment mapFragment;

    private LoggedInCallback mLoggedInCallback;
    private boolean loggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.action_list, android.R.layout.simple_spinner_dropdown_item);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(spinnerAdapter, this);

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
                                    parseUser.put("username", graphUser.getName());
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

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(getIntent());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    @AfterViews
    void setUpView() {
        mLoggedInCallback = mapFragment;
        if (loggedIn && mLoggedInCallback != null) {
            mLoggedInCallback.onLoggedIn();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryRefinementEnabled(true);
        return true;
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

    void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i(LOG_TAG, query);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    PeopleSuggestions.AUTHORITY, PeopleSuggestions.MODE);
            suggestions.saveRecentQuery(query, null);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        return false;
    }
}
