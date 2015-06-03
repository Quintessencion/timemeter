package com.simbirsoft.timemeter.ui.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTaskTimeSpanJob;
import com.simbirsoft.timemeter.ui.base.BaseDialogFragment;
import com.simbirsoft.timemeter.ui.base.FragmentContainerCallbacks;
import com.simbirsoft.timemeter.ui.views.DateTimeView;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.androidannotations.annotations.InstanceState;

import java.util.Calendar;

import javax.inject.Inject;

public class EditTaskActivityDialogFragment extends BaseDialogFragment implements JobLoader.JobLoaderCallbacks,
                    DateTimeView.DateTimeViewListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    public static final String EXTRA_SPAN_ID = "extra_span_id";
    public static final int RESULT_CODE_OK = 10001;
    public static final int RESULT_CODE_CANCELLED = 10002;
    public static final String RESULT_START_MILLIS = "result_start_millis";
    public static final String RESULT_END_MILLIS = "result_end_millis";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String LOAD_SPAN_JOB = "load_span_job";
    public static final int CREATE_NEW_SPAN_ID = -1;
    private static final String TAG_DATE_PICKER_FRAGMENT = "edit_activity_date_picker_fragment_tag";
    private static final String TAG_TIME_PICKER_FRAGMENT = "edit_activity_time_picker_fragment_tag";

    private MaterialDialog mDialog;
    private Long mExtraSpanId;
    private String mExtraTitle;
    private FragmentContainerCallbacks mContainerCallbacks;
    private DateTimeView mStartDateTimeView;
    private DateTimeView mEndDateTimeView;
    private DateTimeView mSelectedDateTimeView;

    long mStartDateTimeMillis;
    long mEndDateTimeMillis;

    @Inject
    LoadTaskTimeSpanJob mLoadSpanJob;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injection.sUiComponent.injectEditTaskActivityDialogFragment(this);

        mExtraTitle = getArguments().getString(EXTRA_TITLE);
        if (mExtraSpanId != null) {
            mExtraSpanId = getArguments().getLong(EXTRA_SPAN_ID);
            mLoadSpanJob.setTaskTimeSpanId(mExtraSpanId);
            requestLoad(LOAD_SPAN_JOB, this);
        } else if (savedInstanceState != null) {
            mStartDateTimeMillis = savedInstanceState.getLong("asd001");
            mEndDateTimeMillis = savedInstanceState.getLong("asd002");
        } else {
            setDefaultSpan();
        }
    }

    private void setDefaultSpan() {
        Calendar c = Calendar.getInstance();
        mEndDateTimeMillis = c.getTimeInMillis();
        //mEndDateTimeView.setDateTimeInMillis(time);
        c.add(Calendar.HOUR, -1);
        mStartDateTimeMillis = c.getTimeInMillis();
        //mStartDateTimeView.setDateTimeInMillis(time2);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof FragmentContainerCallbacks) {
            mContainerCallbacks = (FragmentContainerCallbacks) activity;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        setResult(RESULT_CODE_CANCELLED, null);
        finish();
    }

    @OnJobSuccess(LoadTaskTimeSpanJob.class)
    public void onSpanLoaded(LoadJobResult<TaskTimeSpan> result) {
        mStartDateTimeView.setDateTimeInMillis(result.getData().getStartTimeMillis());
        mEndDateTimeView.setDateTimeInMillis(result.getData().getEndTimeMillis());
    }

    @OnJobFailure(LoadTaskTimeSpanJob.class)
    public void onSpanLoadFailed(JobEvent event) {
        //TODO show error alert
    }

    @Override
    public Job onCreateJob(String tag) {
        switch (tag) {
            case LOAD_SPAN_JOB:
                return mLoadSpanJob;
            default:
                break;
        }
        throw new IllegalArgumentException("undefined tag");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View root = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_edit_task_activity, null);

        mStartDateTimeView = (DateTimeView)root.findViewById(R.id.startDateTime);
        mEndDateTimeView = (DateTimeView)root.findViewById(R.id.endDateTime);

        mStartDateTimeView.setDateTimeInMillis(mStartDateTimeMillis);
        mEndDateTimeView.setDateTimeInMillis(mEndDateTimeMillis);

        mStartDateTimeView.setDateTimeViewListener(this);
        mEndDateTimeView.setDateTimeViewListener(this);



        if (mExtraSpanId == null) {
            setDefaultSpan();
        }

        mDialog = new MaterialDialog.Builder(getActivity())
                .title(mExtraTitle)
                .positiveText(R.string.action_accept)
                .negativeText(R.string.action_cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent result = new Intent();
                        result.putExtra(RESULT_START_MILLIS, mStartDateTimeView.getDateTimeInMillis());
                        result.putExtra(RESULT_END_MILLIS, mEndDateTimeView.getDateTimeInMillis());
                        setResult(RESULT_CODE_OK, result);
                        finish();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        mDialog.dismiss();
                        if (mContainerCallbacks != null) {
                            mContainerCallbacks.finish();
                        }
                    }
                })
                .autoDismiss(false)
                .customView(root, false)
                .build();

        return mDialog;
    }

    @Override
    public void onDateTextClicked(DateTimeView v, Calendar selectedDate) {
        mSelectedDateTimeView = v;
        DatePickerDialog dialog = DatePickerDialog.newInstance(
                this,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH),
                false);
        dialog.show(getChildFragmentManager(), TAG_DATE_PICKER_FRAGMENT);
    }

    @Override
    public void onTimeTextClicked(DateTimeView v, Calendar selectedTime) {
        mSelectedDateTimeView = v;
        TimePickerDialog dialog = TimePickerDialog.newInstance(
                this,
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE),
                false
        );
        dialog.setVibrate(false);
        dialog.show(getChildFragmentManager(), TAG_TIME_PICKER_FRAGMENT);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        if (mSelectedDateTimeView == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        long millis = calendar.getTimeInMillis() + mSelectedDateTimeView.getTimeValueInMillis();
        setSelectedDateTimeValue(millis);
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hour, int minute) {
        if (mSelectedDateTimeView == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mSelectedDateTimeView.getDateValueInMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        setSelectedDateTimeValue(calendar.getTimeInMillis());
    }

    private void setSelectedDateTimeValue(long millis) {
        long start, end;
        if (mSelectedDateTimeView == mStartDateTimeView) {
            start = millis;
            end = mEndDateTimeView.getDateTimeInMillis();
        } else {
            if (millis > System.currentTimeMillis()) {
                //TODO show error alert
                return;
            }
            start = mStartDateTimeView.getDateTimeInMillis();
            end = millis;
        }
        if (end > start) {
            mSelectedDateTimeView.setDateTimeInMillis(millis);
        } else {
            //TODO show error alert
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("asd001", mStartDateTimeView.getDateTimeInMillis());
        outState.putLong("asd002", mEndDateTimeView.getDateTimeInMillis());
    }
}
