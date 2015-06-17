package com.simbirsoft.timemeter.ui.settings;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.support.v4.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.controller.HelpCardController;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.Preferences;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTaskListJob;
import com.simbirsoft.timemeter.ui.base.AppAlertDialogFragment;
import com.simbirsoft.timemeter.ui.base.DialogContainerActivity;
import com.simbirsoft.timemeter.ui.main.SectionFragment;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.util.TimerTextFormatter;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.res.StringRes;

import java.util.List;

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

    Preference mExportStatistics;
    Preference mImportStatistics;

    Preference mDeleteDemo;
    Preference mResetHelp;
    Preference mShowHelp;

    Preferences mPrefs;

    @InstanceState
    TimePickerDialogType mTimePickerDialogType;

    @Inject
    HelpCardController mHelpCardController;

    @Inject
    DatabaseHelper mDatabaseHelper;

    @Inject
    Resources mResources;

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

        mExportStatistics = getPreferenceScreen().findPreference(PreferenceKeys.PREF_EXPORT_STATISTICS);
        mImportStatistics = getPreferenceScreen().findPreference(PreferenceKeys.PREF_IMPORT_STATISTICS);

        mDeleteDemo = getPreferenceScreen().findPreference(PreferenceKeys.PREF_DELETE_DEMO_KEY);
        mResetHelp = getPreferenceScreen().findPreference(PreferenceKeys.PREF_RESET_HELP_KEY);
        mShowHelp = getPreferenceScreen().findPreference(PreferenceKeys.PREF_SHOW_HELP_KEY);

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

        mExportStatistics.setOnPreferenceClickListener(preference -> {
            saveBackup();
            return true;
        });

        mImportStatistics.setOnPreferenceClickListener(preference -> {
            // TODO: impl export last backup
            return true;
        });

        if (mPrefs.getIsDemoTasksDeleted()) {
            hideDeleteDemoPreference();
        }

        mDeleteDemo.setOnPreferenceClickListener(preference -> {
            showDeleteTestDataDialog();
            return true;
        });

        mResetHelp.setOnPreferenceClickListener(preference -> {
            showResetHelpDialog();
            return true;
        });

        mShowHelp.setOnPreferenceChangeListener((preference, newValue) -> {
            mPrefs.setIsShowHelp((Boolean) newValue);
            return true;
        });
    }

    @AfterViews
    public void initDefaultValues() {
        mStartTimePreference.setSummary(
                getFormattedTime(mCalendarStartTimeSummary, mPrefs.getDayStartHour()));
        mEndTimePreference.setSummary(
                getFormattedTime(mCalendarEndTimeSummary, mPrefs.getDayEndHour()));
        mDisplayAllActivities.setDefaultValue(mPrefs.getDisplayAllActivities());

        TimePickerDialog timePickerDialog = (TimePickerDialog) getChildFragmentManager().findFragmentByTag(TIME_PICKER_DIALOG_TAG);
        if (timePickerDialog != null) {
            timePickerDialog.setOnTimeSetListener(this);
        }

        setActionBarTitleAndHome(mResources.getString(R.string.title_settings));
    }

    private void setActionBarTitleAndHome(String title) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        if (title != null) {
            actionBar.setTitle(title);
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
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
                    showErrorSnackBarDialog();
                }
                break;
            case END_TIME_PICKER_DIALOG:
                if (hours >= currentStartHour) {
                    mEndTimePreference.setSummary(getFormattedTime(mCalendarEndTimeSummary, hours));
                    mPrefs.setDayEndHour(hours);
                } else {
                    showErrorSnackBarDialog();
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
            hideDeleteDemoPreference();
            mPrefs.setIsDemoTasksDeleted(true);
            mDatabaseHelper.removeTestData();
            setPreferencesModified();
        }
    }

    @OnActivityResult(REQUEST_CODE_RESET_HELP)
    public void onResetHelpDialogResult(int resultCode) {
        if (resultCode == AppAlertDialogFragment.RESULT_CODE_ACCEPTED) {
            mHelpCardController.markAllUnpresented();
        }
    }

    private void showTimePickerDialog(int hour) {
        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this,
                hour,
                0,
                false,
                false);

        timePickerDialog.show(getChildFragmentManager(), TIME_PICKER_DIALOG_TAG);
    }

    private String getFormattedTime(String summary, int hours) {
        return Html.fromHtml(String.format(summary,
                TimerTextFormatter.formatHoursText(mResources, hours))).toString();
    }

    private void showErrorSnackBarDialog() {
        Snackbar bar = Snackbar.with(getActivity())
                .type(SnackbarType.MULTI_LINE)
                .text(R.string.settings_incorrect_time)
                .colorResource(R.color.lightRed)
                .duration(Snackbar.SnackbarDuration.LENGTH_LONG);
        SnackbarManager.show(bar);
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

    private void hideDeleteDemoPreference() {
        PreferenceCategory mCategory = (PreferenceCategory) findPreference(PreferenceKeys.PREF_CATEGORY_DELETE_DEMO_KEY);
        if (mCategory != null) {
            mCategory.removePreference(mDeleteDemo);
        }
    }

    private void setPreferencesModified() {
        ((SettingsActivity)getActivity()).setSettingsModified();
    }

    private void saveBackup() {
        ((SettingsActivity)getActivity()).loadTasks();

        /*getChildFragmentManager()
                .beginTransaction()
                .attach(loadTasksFragment)
                .commit(); */

        //loadTasksFragment.loadTasks();
    }
}
