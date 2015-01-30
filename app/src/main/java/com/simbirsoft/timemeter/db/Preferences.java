package com.simbirsoft.timemeter.db;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.simbirsoft.timemeter.App;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class Preferences {

    private final SharedPreferences mPrefs;

    public static final String PREFERENCE_DATABASE_TEST_DATA_INITIALIZED = "is_database_test_data_initialized";

    @Inject
    public Preferences(App appContext) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    public boolean isDatabaseTestDataInitialized() {
        return mPrefs.getBoolean(PREFERENCE_DATABASE_TEST_DATA_INITIALIZED, false);
    }

    public void setDatabaseTestDataInitialized(boolean isInitialized) {
        mPrefs.edit().putBoolean(PREFERENCE_DATABASE_TEST_DATA_INITIALIZED, isInitialized).apply();
    }
}
