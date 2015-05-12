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

    private static final String PREFERENCE_DAY_START_HOUR = "day_start_hour";
    private static final String PREFERENCE_DAY_END_HOUR = "day_end_hour";

    private static final int DAY_START_HOUR_DEFAULT = 8;
    private static final int DAY_END_HOUR_DEFAULT = 24;

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

    public int getDayStartHour() {
        return mPrefs.getInt(PREFERENCE_DAY_START_HOUR, DAY_START_HOUR_DEFAULT);
    }

    public void setDayStartHour(int startHour) {
        mPrefs.edit().putInt(PREFERENCE_DAY_START_HOUR, startHour).apply();
    }

    public int getDayEndHour() {
        return mPrefs.getInt(PREFERENCE_DAY_END_HOUR, DAY_END_HOUR_DEFAULT);
    }

    public void setDayEndHour(int endHour) {
        mPrefs.edit().putInt(PREFERENCE_DAY_END_HOUR, endHour).apply();
    }
}
