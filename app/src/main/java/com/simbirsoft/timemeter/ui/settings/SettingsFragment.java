package com.simbirsoft.timemeter.ui.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.main.SectionFragment;
import com.simbirsoft.timemeter.ui.util.TimeUtils;
import com.simbirsoft.timemeter.util.SharedPreferences_;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EFragment
public class SettingsFragment extends PreferenceFragment implements SectionFragment,
        TimePickerDialog.OnTimeSetListener {
    private static final String TIME_PICKER_DIALOG_TAG = "time_picker_dialog_tag";

    @Pref
    SharedPreferences_ mSharedPreference;

    @StringRes(R.string.calendar_summary_start_time)
    String mCalendarStartTimeSummary;

    @StringRes(R.string.calendar_summary_end_time)
    String mCalendarEndTimeSummary;

    Preference startTimePreference;
    Preference endTimePreference;

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

        startTimePreference = getPreferenceScreen().findPreference("pref_startTime");
        endTimePreference = getPreferenceScreen().findPreference("pref_endTime");
        getPreferenceManager().setSharedPreferencesName("SharedPreferences");

        startTimePreference.setOnPreferenceClickListener(preference -> {
            mTimePickerDialogType = TimePickerDialogType.START_TIME_PICKER_DIALOG;
            showTimePickerDialog(mSharedPreference.calendarStartTime().get());
            return true;
        });

        endTimePreference.setOnPreferenceClickListener(preference -> {
            mTimePickerDialogType = TimePickerDialogType.END_TIME_PICKER_DIALOG;
            showTimePickerDialog(mSharedPreference.calendarEndTime().get());
            return true;
        });

        mTimePickerDialogType = TimePickerDialogType.NONE;
    }

    @AfterViews
    public void initDefaultValues() {
        startTimePreference.setSummary(
                getFormattedTime(mCalendarStartTimeSummary, mSharedPreference.calendarStartTime().get()));
        endTimePreference.setSummary(
                getFormattedTime(mCalendarEndTimeSummary, mSharedPreference.calendarEndTime().get()));
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
        int timeInMinutes = TimeUtils.hoursAndMinutesToMinutes(hours, minutes);

        switch (mTimePickerDialogType) {
            case START_TIME_PICKER_DIALOG:
                if (isStartTimeLessThanEndTime(timeInMinutes, mSharedPreference.calendarEndTime().get())) {
                    startTimePreference.setSummary(getFormattedTime(mCalendarStartTimeSummary, timeInMinutes));
                    mSharedPreference.calendarStartTime().put(timeInMinutes);
                } else {
                    showErrorDialog();
                }
                break;
            case END_TIME_PICKER_DIALOG:
                if (isStartTimeLessThanEndTime(mSharedPreference.calendarEndTime().get(), timeInMinutes)) {
                    endTimePreference.setSummary(getFormattedTime(mCalendarEndTimeSummary, timeInMinutes));
                    mSharedPreference.calendarEndTime().put(timeInMinutes);
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

    private String getFormattedTime(String summary, int timeInMinutes) {
        return String.format(summary, TimeUtils.formatMinutes(timeInMinutes));
    }

    private boolean isStartTimeLessThanEndTime(int calendarStartTime, int calendarEndTime) {
        return calendarStartTime <= calendarEndTime;
    }

    private void showErrorDialog() {
        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.settings_incorrect_time)
                .positiveText(R.string.action_accept)
                .build();
        materialDialog.show();
    }
}
