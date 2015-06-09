package com.simbirsoft.timemeter.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.controller.HelpCardController;
import com.simbirsoft.timemeter.db.Preferences;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.ui.base.AppAlertDialogFragment;
import com.simbirsoft.timemeter.ui.base.DialogContainerActivity;
import com.simbirsoft.timemeter.ui.main.SectionFragment;
import com.simbirsoft.timemeter.ui.util.TimeUtils;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.res.StringRes;

import javax.inject.Inject;

@EFragment
public class SettingsFragment extends PreferenceFragment implements SectionFragment,
        TimePickerDialog.OnTimeSetListener {

    private static final String TIME_PICKER_DIALOG_TAG = "time_picker_dialog_tag";
    private static final int REQUEST_CODE_DELETE_TEST_DATA = 10001;
    private static final int REQUEST_CODE_RESET_HELP = 10002;

    @StringRes(R.string.calendar_summary_start_time)
    String mCalendarStartTimeSummary;

    @StringRes(R.string.calendar_summary_end_time)
    String mCalendarEndTimeSummary;

    Preference mStartTimePreference;
    Preference mEndTimePreference;
    Preference mDisplayAllActivities;
    Preference mDeleteDemo;
    Preference mResetHelp;

    Preferences mPrefs;

    @InstanceState
    TimePickerDialogType mTimePickerDialogType;

    @Inject
    HelpCardController mHelpCardController;

    public enum TimePickerDialogType {
        START_TIME_PICKER_DIALOG,
        END_TIME_PICKER_DIALOG,
        NONE
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Injection.sUiComponent.injectSettingsFragment(this);

        mPrefs = Injection.sDatabaseComponent.preferences();

        mStartTimePreference = getPreferenceScreen().findPreference(PreferenceKeys.PREF_START_TIME_KEY);
        mEndTimePreference = getPreferenceScreen().findPreference(PreferenceKeys.PREF_END_TIME_KEY);
        mDisplayAllActivities = getPreferenceScreen().findPreference(PreferenceKeys.PREF_ALL_ACTIVITY_KEY);
        mDeleteDemo = getPreferenceScreen().findPreference(PreferenceKeys.PREF_DELETE_DEMO_KEY);
        mResetHelp = getPreferenceScreen().findPreference(PreferenceKeys.PREF_RESET_HELP_KEY);

        mStartTimePreference.setOnPreferenceClickListener(preference -> {
            mTimePickerDialogType = TimePickerDialogType.START_TIME_PICKER_DIALOG;
            showTimePickerDialog(mPrefs.getDayStartHour());
            return true;
        });

        mEndTimePreference.setOnPreferenceClickListener(preference -> {
            mTimePickerDialogType = TimePickerDialogType.END_TIME_PICKER_DIALOG;
            showTimePickerDialog(mPrefs.getDayEndHour());
            return true;
        });

        mDisplayAllActivities.setOnPreferenceChangeListener((preference, newValue) -> {
            mPrefs.setDisplayAllActivities((Boolean) newValue);
            return true;
        });

        mDeleteDemo.setOnPreferenceClickListener(preference -> {
            showDeleteTestDataDialog();
            return true;
        });

        mResetHelp.setOnPreferenceClickListener(preference -> {
            showResetHelpDialog();
            return true;
        });

        mTimePickerDialogType = TimePickerDialogType.NONE;
    }

    @AfterViews
    public void initDefaultValues() {
        mStartTimePreference.setSummary(
                getFormattedTime(mCalendarStartTimeSummary, mPrefs.getDayStartHour()));
        mEndTimePreference.setSummary(
                getFormattedTime(mCalendarEndTimeSummary, mPrefs.getDayEndHour()));
        mDisplayAllActivities.setDefaultValue(mPrefs.getDisplayAllActivities());
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
                    mStartTimePreference.setSummary(getFormattedTime(mCalendarStartTimeSummary, hours));
                    mPrefs.setDayStartHour(hours);
                } else {
                    showErrorDialog();
                }
                break;
            case END_TIME_PICKER_DIALOG:
                if (hours >= currentStartHour) {
                    mEndTimePreference.setSummary(getFormattedTime(mCalendarEndTimeSummary, hours));
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

    @OnActivityResult(REQUEST_CODE_DELETE_TEST_DATA)
     public void onDeleteTestDataDialogResult(int resultCode) {
        if (resultCode == AppAlertDialogFragment.RESULT_CODE_ACCEPTED) {
            mPrefs.setIsDemoTasksDeleted(true);
        }
    }

    @OnActivityResult(REQUEST_CODE_RESET_HELP)
    public void onResetHelpDialogResult(int resultCode) {
        if (resultCode == AppAlertDialogFragment.RESULT_CODE_ACCEPTED) {
            mHelpCardController.markAllUnpresented();
        }
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

    private void showDeleteTestDataDialog() {
        Bundle args = AppAlertDialogFragment.prepareArgs(getActivity(),
                R.string.dialog_delete_test_data_title,
                R.string.dialog_delete_test_data,
                R.string.action_proceed,
                R.string.action_cancel);
        Intent launchIntent = DialogContainerActivity.prepareDialogLaunchIntent(
                getActivity(),
                AppAlertDialogFragment.class.getName(),
                args);
        getActivity().startActivityForResult(launchIntent,
                REQUEST_CODE_DELETE_TEST_DATA);
    }

    private void showResetHelpDialog() {
        Bundle args = AppAlertDialogFragment.prepareArgs(getActivity(),
                R.string.dialog_delete_test_data_title,
                R.string.dialog_reset_help,
                R.string.action_proceed,
                R.string.action_cancel);
        Intent launchIntent = DialogContainerActivity.prepareDialogLaunchIntent(
                getActivity(),
                AppAlertDialogFragment.class.getName(),
                args);
        getActivity().startActivityForResult(launchIntent,
                REQUEST_CODE_RESET_HELP);
    }
}
