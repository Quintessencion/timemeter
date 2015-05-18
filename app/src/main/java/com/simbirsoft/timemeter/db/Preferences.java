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
    public static final String PREFERENCE_STATE_SELECTED_POSITION = "state_selection_position";
    public static final String PREFERENCE_TAG_PAGE_POSITION = "tag_page_position";

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
}
