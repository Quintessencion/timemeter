package com.simbirsoft.timemeter.db;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.simbirsoft.timemeter.App;
import com.simbirsoft.timemeter.ui.util.MarshallUtils;
import com.simbirsoft.timemeter.ui.views.FilterView;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class Preferences {

    private final SharedPreferences mPrefs;

    public static final String PREFERENCE_DATABASE_TEST_DATA_INITIALIZED = "is_database_test_data_initialized";

    private static final String KEY_FILTER_STATE = "filter_state";
    private static final String KEY_IS_FILTER_PANEL_SHOWN = "is_filter_panel_shown";

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

    public FilterView.FilterState getFilterState() {
        FilterView.FilterState filterState = null;
        try {
            if (mPrefs.contains(KEY_FILTER_STATE)) {
                String value = mPrefs.getString(KEY_FILTER_STATE, "");
                byte[] arr = Base64.decode(value, Base64.DEFAULT);
                filterState = MarshallUtils.unmarshall(arr, FilterView.FilterState.CREATOR);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return filterState;
    }

    public void setFilterState(FilterView.FilterState filterState) {
        try {
            if (filterState != null) {
                byte[] arr = MarshallUtils.marshall(filterState);
                String value = Base64.encodeToString(arr, Base64.DEFAULT);
                mPrefs.edit().putString(KEY_FILTER_STATE, value).apply();
            } else {
                mPrefs.edit().remove(KEY_FILTER_STATE).apply();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
