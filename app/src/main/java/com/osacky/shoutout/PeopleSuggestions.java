package com.osacky.shoutout;

import android.content.SearchRecentSuggestionsProvider;

public class PeopleSuggestions extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = "com.osacky.shoutout.PeopleSuggestions";
    public static final int MODE = DATABASE_MODE_QUERIES;

    public PeopleSuggestions() {
        setupSuggestions(AUTHORITY, MODE);
    }
}

