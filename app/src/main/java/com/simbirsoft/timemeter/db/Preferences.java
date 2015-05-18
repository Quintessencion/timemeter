package com.simbirsoft.timemeter.db;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.App;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class Preferences {

    private final SharedPreferences mPrefs;

    public static final String PREFERENCE_DATABASE_TEST_DATA_INITIALIZED = "is_database_test_data_initialized";
    public static final String PREFERENCE_STATE_SELECTED_POSITION = "state_selection_position";
    public static final String PREFERENCE_TAG_PAGE_POSITION = "tag_page_position";

    private static final String PREFERENCE_DAY_START_HOUR = "day_start_hour";
    private static final String PREFERENCE_DAY_END_HOUR = "day_end_hour";

    private static final int DAY_START_HOUR_DEFAULT = 8;
    private static final int DAY_END_HOUR_DEFAULT = 24;
    private static final int DAY_MIN_HOUR = 0;
    private static final int DAY_MAX_HOUR = 24;

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

    public void setPreferenceTagPagePosition(int tagPagePosition) {
        mPrefs.edit().putInt(PREFERENCE_TAG_PAGE_POSITION, tagPagePosition).apply();
    }

    public int getPreferenceTagPagePosition(int defaultValue) {
        return mPrefs.getInt(PREFERENCE_TAG_PAGE_POSITION, defaultValue);
    }

    public void setPreferenceStateSelectedPosition(int stateSelectedPosition) {
        mPrefs.edit().putInt(PREFERENCE_STATE_SELECTED_POSITION, stateSelectedPosition);
    }

    public int getPreferenceStateSelectedPosition(int defaultValue) {
        return mPrefs.getInt(PREFERENCE_STATE_SELECTED_POSITION, defaultValue);
    }

    public int getDayStartHour() {
        return mPrefs.getInt(PREFERENCE_DAY_START_HOUR, DAY_START_HOUR_DEFAULT);
    }

    public void setDayStartHour(int startHour) {
        Preconditions.checkArgument(checkHourValue(startHour), "day start hour is out of bounds");
        int endHour = getDayEndHour();
        Preconditions.checkArgument(startHour < endHour, "date start hour should be less than day end hour");
        mPrefs.edit().putInt(PREFERENCE_DAY_START_HOUR, startHour).apply();
    }

    public int getDayEndHour() {
        return mPrefs.getInt(PREFERENCE_DAY_END_HOUR, DAY_END_HOUR_DEFAULT);
    }

    public void setDayEndHour(int endHour) {
        Preconditions.checkArgument(checkHourValue(endHour), "day end hour is out of bounds");
        int startHour = getDayStartHour();
        Preconditions.checkArgument(endHour > startHour, "day end hour should be greater than day start hour");
        mPrefs.edit().putInt(PREFERENCE_DAY_END_HOUR, endHour).apply();
    }

    private boolean checkHourValue(int hour) {
        return hour >= DAY_MIN_HOUR && hour <= DAY_MAX_HOUR;
    }
}
