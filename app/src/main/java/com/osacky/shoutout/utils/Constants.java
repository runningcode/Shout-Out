package com.osacky.shoutout.utils;

import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;

import org.joda.time.DateTimeConstants;

@SuppressWarnings("unused")
final public class Constants {

    private Constants() {
        throw new AssertionError();
    }

    public static final String ARG_SECTION_NUMBER = "section_number";
    public static final String FIREBASE_URL = "https://shoutout.firebaseio.com";
    public static final String CONNECTION_URL = "https://shoutout.firebaseio.com/.info/connected";
    public static final String USERS_PATH = "users";
    public static final String LOC_PATH = "loc";
    public static final String STATUS_PATH = "status";
    public static final String USER_URL = FIREBASE_URL + "/" + USERS_PATH;
    public static final String LOC_URL = FIREBASE_URL + "/" + LOC_PATH;
    public static final String STATUS_URL = FIREBASE_URL + "/" + STATUS_PATH;

    public static final String PARSE_ID = "S5HVjNqmiwUgiGjMDiJLYh361p5P7Ob3fCOabrJ9";
    public static final String PARSE_SECRET = "3GWNcqZ7LJhBtGbbmQfs0ROHKFM5sX6GDT9IWhCk";

    public static final int UPDATE_INTERVAL = 20 * DateTimeConstants.MILLIS_PER_SECOND;
    public static final int FASTEST_INTERVAL = 10 * DateTimeConstants.MILLIS_PER_SECOND;


    public static SpringSystem springSystem = SpringSystem.create();
    public static final SpringConfig ORIGAMI_SPRING_CONFIG = SpringConfig
            .fromOrigamiTensionAndFriction(40, 5);
    public static final SpringConfig BUTTON_SPRING_CONFIG = SpringConfig
            .fromOrigamiTensionAndFriction(140, 8);
}
