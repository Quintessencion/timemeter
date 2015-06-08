package com.simbirsoft.timemeter.db;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.App;
import com.simbirsoft.timemeter.ui.views.FilterView;
import com.simbirsoft.timemeter.util.MarshallUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class Preferences {

    private final SharedPreferences mPrefs;

    public static final String PREFERENCE_DATABASE_TEST_DATA_INITIALIZED = "is_database_test_data_initialized";
    public static final String PREFERENCE_SELECTED_SECTION_POSITION = "state_selection_position";
    public static final String PREFERENCE_SELECTED_TASK_TAB_POSITION = "tag_page_position";
    public static final String PREFERENCE_USER_LEARNED_DRAWER = "user_learned_drawer";

    private static final String KEY_FILTER_STATE = "filter_state";
    private static final String KEY_PRESENTED_HELP_CARDS = "presented_help_cards";

    private static final String PREFERENCE_DAY_START_HOUR = "day_start_hour";
    private static final String PREFERENCE_DAY_END_HOUR = "day_end_hour";

    private static final int DAY_START_HOUR_DEFAULT = 8;
    private static final int DAY_END_HOUR_DEFAULT = 24;
    private static final int DAY_MIN_HOUR = 0;
    private static final int DAY_MAX_HOUR = 24;

    private static final String PREFERENCE_SHOW_ALL_ACTIVITY = "show_all_activity";

    private static final boolean SHOW_ALL_ACTIVITY_DEFAULT = true;

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

    public void setSelectedTaskTabPosition(int tagPagePosition) {
        mPrefs.edit().putInt(PREFERENCE_SELECTED_TASK_TAB_POSITION, tagPagePosition).apply();
    }

    public int getSelectedTaskTabPosition() {
        return mPrefs.getInt(PREFERENCE_SELECTED_TASK_TAB_POSITION, 0);
    }

    public void setSelectedSectionPosition(int stateSelectedPosition) {
        mPrefs.edit().putInt(PREFERENCE_SELECTED_SECTION_POSITION, stateSelectedPosition).apply();
    }

    public int getSelectedSectionPosition() {
        return mPrefs.getInt(PREFERENCE_SELECTED_SECTION_POSITION, 0);
    }

    public boolean getUserLearnedDrawer() {
        return mPrefs.getBoolean(PREFERENCE_USER_LEARNED_DRAWER, false);
    }

    public void setUserLearnedDrawer(boolean userLearnedDrawer) {
        mPrefs.edit().putBoolean(PREFERENCE_USER_LEARNED_DRAWER, userLearnedDrawer).apply();
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

    public boolean getDisplayAllActivities() {
        return mPrefs.getBoolean(PREFERENCE_SHOW_ALL_ACTIVITY, SHOW_ALL_ACTIVITY_DEFAULT);
    }

    public void setDisplayAllActivities(boolean isShowAllActivity) {
        mPrefs.edit().putBoolean(PREFERENCE_SHOW_ALL_ACTIVITY, isShowAllActivity).apply();
    }

    private boolean checkHourValue(int hour) {
        return hour >= DAY_MIN_HOUR && hour <= DAY_MAX_HOUR;
    }

    public Integer[] getPresentedHelpCards() {
        Integer[] ids = new Integer[0];
        try {
            if (mPrefs.contains(KEY_PRESENTED_HELP_CARDS)) {
                String value = mPrefs.getString(KEY_PRESENTED_HELP_CARDS, "");
                byte[] arr = Base64.decode(value, Base64.DEFAULT);
                ids = MarshallUtils.unmarshall(arr, ids);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ids;
    }

    public void setPresentedHelpCards(Integer[] cardIds) {
        try {
            if (cardIds != null) {
                byte[] arr = MarshallUtils.marshall(cardIds);
                String value = Base64.encodeToString(arr, Base64.DEFAULT);
                mPrefs.edit().putString(KEY_PRESENTED_HELP_CARDS, value).apply();
            } else {
                mPrefs.edit().remove(KEY_PRESENTED_HELP_CARDS).apply();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
