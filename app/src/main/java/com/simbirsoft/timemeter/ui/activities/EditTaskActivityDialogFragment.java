package com.simbirsoft.timemeter.ui.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

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
import com.simbirsoft.timemeter.jobs.UpdateTaskTimeSpanJob;
import com.simbirsoft.timemeter.ui.base.BaseDialogFragment;
import com.simbirsoft.timemeter.ui.base.FragmentContainerCallbacks;
import com.simbirsoft.timemeter.ui.views.DateTimeView;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.androidannotations.annotations.InstanceState;
import org.w3c.dom.Text;

import java.util.Calendar;

import javax.inject.Inject;

public class EditTaskActivityDialogFragment extends BaseDialogFragment implements JobLoader.JobLoaderCallbacks,
                    DateTimeView.DateTimeViewListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    public static final String EXTRA_SPAN_ID = "extra_span_id";
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final int RESULT_CODE_OK = 10001;
    public static final int RESULT_CODE_CANCELLED = 10002;
    public static final String EXTRA_TITLE = "extra_title";
    public static final String LOAD_SPAN_JOB = "load_span_job";
    public static final String UPDATE_SPAN_JOB = "update_span_job";
    public static final int CREATE_NEW_SPAN_ID = -1;
    private static final String TAG_DATE_PICKER_FRAGMENT = "edit_activity_date_picker_fragment_tag";
    private static final String TAG_TIME_PICKER_FRAGMENT = "edit_activity_time_picker_fragment_tag";
    private static final String STATE_SPAN = "state_span";

    private MaterialDialog mDialog;
    private Long mExtraSpanId;
    private String mExtraTitle;
    private FragmentContainerCallbacks mContainerCallbacks;
    private DateTimeView mStartDateTimeView;
    private DateTimeView mEndDateTimeView;
    private DateTimeView mSelectedDateTimeView;
    private TextView mErrorMessage;
    private TaskTimeSpan mSpan;

    @Inject
    LoadTaskTimeSpanJob mLoadSpanJob;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injection.sUiComponent.injectEditTaskActivityDialogFragment(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mExtraTitle = getArguments().getString(EXTRA_TITLE);
        mExtraSpanId = getArguments().getLong(EXTRA_SPAN_ID);

        if (mExtraSpanId != CREATE_NEW_SPAN_ID) {
            mLoadSpanJob.setTaskTimeSpanId(mExtraSpanId);
            requestLoad(LOAD_SPAN_JOB, this);
        } else {
            if (savedInstanceState == null) {
                setDefaultSpan();
            } else {
                mSpan = savedInstanceState.getParcelable(STATE_SPAN);
            }

            updateFields();
        }
    }

    private void updateFields() {
        mStartDateTimeView.setDateTimeInMillis(mSpan.getStartTimeMillis());
        mEndDateTimeView.setDateTimeInMillis(mSpan.getEndTimeMillis());
    }

    private void setDefaultSpan() {
        mSpan = new TaskTimeSpan();
        mSpan.setTaskId(getArguments().getLong(EXTRA_TASK_ID));
        Calendar c = Calendar.getInstance();
        mSpan.setEndTimeMillis(c.getTimeInMillis());
        c.add(Calendar.HOUR, -1);
        mSpan.setStartTimeMillis(c.getTimeInMillis());
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
        mSpan = result.getData();
        updateFields();
    }

    @OnJobFailure(LoadTaskTimeSpanJob.class)
    public void onSpanLoadFailed(JobEvent event) {
        //TODO show error alert
    }

    @OnJobSuccess(UpdateTaskTimeSpanJob.class)
    public void onUpdateSpanSuccess(JobEvent ev) {
        mErrorMessage.setVisibility(View.GONE);

        setResult(RESULT_CODE_OK, null);
        finish();
    }

    @OnJobFailure(UpdateTaskTimeSpanJob.class)
    public void onUpdateSpanFailed(JobEvent ev) {
        String msg = getSpanEditErrorDescription(ev.getEventCode());

        mErrorMessage.setText(msg);
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    private String getSpanEditErrorDescription(int errorCode) {
        switch (errorCode) {
            case UpdateTaskTimeSpanJob.ERROR_BAD_RANGE :
                return getString(R.string.error_time_span_bad_range);
            case UpdateTaskTimeSpanJob.ERROR_BELONGS_TO_FUTURE:
                return getString(R.string.error_time_span_belongs_to_future);
            case UpdateTaskTimeSpanJob.ERROR_OVERLAPS:
                return getString(R.string.error_time_span_overlaps);
            default:
                return getString(R.string.error_unknown);
        }
    }

    @Override
    public Job onCreateJob(String tag) {
        switch (tag) {
            case LOAD_SPAN_JOB:
                LoadTaskTimeSpanJob job = Injection.sJobsComponent.loadTaskTimeSpanJob();
                job.setTaskTimeSpanId(mExtraSpanId);
                return job;
            case UPDATE_SPAN_JOB:
                UpdateTaskTimeSpanJob updJob = Injection.sJobsComponent.updateTaskTimeSpanJob();
                updJob.setSpan(mSpan);
                return updJob;
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
        mErrorMessage = (TextView)root.findViewById(R.id.errorMessage);

        mStartDateTimeView.setDateTimeViewListener(this);
        mEndDateTimeView.setDateTimeViewListener(this);

        EditTaskActivityDialogFragment self = this;
        mDialog = new MaterialDialog.Builder(getActivity())
                .title(mExtraTitle)
                .positiveText(R.string.action_accept)
                .negativeText(R.string.action_cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        mSpan.setStartTimeMillis(mStartDateTimeView.getDateTimeInMillis());
                        mSpan.setEndTimeMillis(mEndDateTimeView.getDateTimeInMillis());

                        requestLoad(UPDATE_SPAN_JOB, self);
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
        calendar.setTimeInMillis(mSelectedDateTimeView.getDateTimeInMillis());
        calendar.set(year, month, day);
        mSelectedDateTimeView.setDateTimeInMillis(calendar.getTimeInMillis());
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hour, int minute) {
        if (mSelectedDateTimeView == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mSelectedDateTimeView.getDateTimeInMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        mSelectedDateTimeView.setDateTimeInMillis(calendar.getTimeInMillis());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_SPAN, mSpan);
    }
}
