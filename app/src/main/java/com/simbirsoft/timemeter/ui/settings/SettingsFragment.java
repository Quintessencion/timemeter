package com.simbirsoft.timemeter.ui.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.Preferences;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.ui.main.SectionFragment;
import com.simbirsoft.timemeter.ui.util.TimeUtils;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.res.StringRes;

@EFragment
public class SettingsFragment extends PreferenceFragment implements SectionFragment,
        TimePickerDialog.OnTimeSetListener {

    private static final String TIME_PICKER_DIALOG_TAG = "time_picker_dialog_tag";

    @StringRes(R.string.calendar_summary_start_time)
    String mCalendarStartTimeSummary;

    @StringRes(R.string.calendar_summary_end_time)
    String mCalendarEndTimeSummary;

    Preference startTimePreference;
    Preference endTimePreference;
    Preference displayAllActivities;

    Preferences mPrefs;

    @InstanceState
    TimePickerDialogType mTimePickerDialogType;

    public enum TimePickerDialogType {
        START_TIME_PICKER_DIALOG,
        END_TIME_PICKER_DIALOG,
        NONE
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mPrefs = Injection.sDatabaseComponent.preferences();

        startTimePreference = getPreferenceScreen().findPreference(PreferenceKeys.PREF_START_TIME_KEY);
        endTimePreference = getPreferenceScreen().findPreference(PreferenceKeys.PREF_END_TIME_KEY);
        displayAllActivities = getPreferenceScreen().findPreference(PreferenceKeys.PREF_ALL_ACTIVITY_KEY);

        startTimePreference.setOnPreferenceClickListener(preference -> {
            mTimePickerDialogType = TimePickerDialogType.START_TIME_PICKER_DIALOG;
            showTimePickerDialog(mPrefs.getDayStartHour());
            return true;
        });

        endTimePreference.setOnPreferenceClickListener(preference -> {
            mTimePickerDialogType = TimePickerDialogType.END_TIME_PICKER_DIALOG;
            showTimePickerDialog(mPrefs.getDayEndHour());
            return true;
        });

        displayAllActivities.setOnPreferenceChangeListener((preference, newValue) -> {
            mPrefs.setDisplayAllActivities((Boolean) newValue);
            return true;
        });


        mTimePickerDialogType = TimePickerDialogType.NONE;
    }

    @AfterViews
    public void initDefaultValues() {
        startTimePreference.setSummary(
                getFormattedTime(mCalendarStartTimeSummary, mPrefs.getDayStartHour()));
        endTimePreference.setSummary(
                getFormattedTime(mCalendarEndTimeSummary, mPrefs.getDayEndHour()));
        displayAllActivities.setDefaultValue(mPrefs.getDisplayAllActivities());
    }

    @Override
    public int getSectionId() {
        Bundle args = getArguments();

        if (args == null || !args.containsKey(ARG_SECTION_ID)) {
            return -1;
        }

        return args.getInt(ARG_SECTION_ID);
    }

    @Override
    public String getFragmentStateKey() {
        return "_state_" + getClass().getSimpleName();
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hours, int minutes) {
        int currentStartHour = mPrefs.getDayStartHour();
        int currentEndHour = mPrefs.getDayEndHour();

        switch (mTimePickerDialogType) {
            case START_TIME_PICKER_DIALOG:
                if (hours <= currentEndHour) {
                    startTimePreference.setSummary(getFormattedTime(mCalendarStartTimeSummary, hours));
                    mPrefs.setDayStartHour(hours);
                } else {
                    showErrorDialog();
                }
                break;
            case END_TIME_PICKER_DIALOG:
                if (hours >= currentStartHour) {
                    endTimePreference.setSummary(getFormattedTime(mCalendarEndTimeSummary, hours));
                    mPrefs.setDayEndHour(hours);
                } else {
                    showErrorDialog();
                }
                break;
            case NONE:
                break;
        }

        mTimePickerDialogType = TimePickerDialogType.NONE;
    }

    private void showTimePickerDialog(int minutes) {
        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this,
                TimeUtils.getHoursFromMinutes(minutes),
                TimeUtils.getMinutesFromMinutes(minutes),
                false,
                false);

        timePickerDialog.show(getChildFragmentManager(), TIME_PICKER_DIALOG_TAG);
    }

    private String getFormattedTime(String summary, int hours) {
        return String.format(summary, hours + ":00");
    }

    private void showErrorDialog() {
        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.settings_incorrect_time)
                .positiveText(R.string.action_accept)
                .build();
        materialDialog.show();
    }
}
