package com.osacky.shoutout;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;

public class ShoutOutApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, Constants.PARSE_ID, Constants.PARSE_SECRET);
        ParseFacebookUtils.initialize(getString(R.string.fb_app_id));
    }
}
