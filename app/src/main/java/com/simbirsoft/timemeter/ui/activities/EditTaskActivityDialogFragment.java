package com.simbirsoft.timemeter.ui.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
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
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTaskTimeSpanJob;
import com.simbirsoft.timemeter.ui.base.BaseDialogFragment;
import com.simbirsoft.timemeter.ui.base.FragmentContainerCallbacks;
import com.simbirsoft.timemeter.ui.views.DateTimeView;

import javax.inject.Inject;

public class EditTaskActivityDialogFragment extends BaseDialogFragment implements JobLoader.JobLoaderCallbacks {
    public static final String EXTRA_SPAN_ID = "extra_span_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String LOAD_SPAN_JOB = "load_span_job";

    private MaterialDialog mDialog;
    private Long mExtraSpanId;
    private String mExtraTitle;
    private FragmentContainerCallbacks mContainerCallbacks;
    private DateTimeView mStartDateTimeView;
    private DateTimeView mEndDateTimeView;

    @Inject
    LoadTaskTimeSpanJob mLoadSpanJob;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injection.sUiComponent.injectEditTaskActivityDialogFragment(this);
        mExtraSpanId = getArguments().getLong(EXTRA_SPAN_ID);
        mExtraTitle = getArguments().getString(EXTRA_TITLE);
        Preconditions.checkArgument(mExtraSpanId != null);
        mLoadSpanJob.setTaskTimeSpanId(mExtraSpanId);
        requestLoad(LOAD_SPAN_JOB, this);
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

        if (mContainerCallbacks != null) {
            mContainerCallbacks.finish();
        }
    }

    @OnJobSuccess(LoadTaskTimeSpanJob.class)
    public void onSpanLoaded(LoadJobResult<TaskTimeSpan> result) {
        mStartDateTimeView.setDateTimeInMillis(result.getData().getStartTimeMillis());
        mEndDateTimeView.setDateTimeInMillis(result.getData().getEndTimeMillis());
    }

    @OnJobFailure(LoadTaskTimeSpanJob.class)
    public void onSpanLoadFailed(JobEvent event) {

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

        mDialog = new MaterialDialog.Builder(getActivity())
                .title(mExtraTitle)
                .positiveText(R.string.action_accept)
                .negativeText(R.string.action_cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {

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
}
