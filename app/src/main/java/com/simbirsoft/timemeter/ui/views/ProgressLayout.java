package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.util.JobSelector;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.simbirsoft.timemeter.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EViewGroup(R.layout.view_progress_layout)
public class ProgressLayout extends RelativeLayout {

    public interface ProgressLayoutCallbacks {
        public boolean hasContent();
        public boolean isInProgress();
    }

    public static abstract class JobProgressLayoutCallbacks implements ProgressLayoutCallbacks {

        private final JobSelector mJobSelector;

        public JobProgressLayoutCallbacks(JobSelector selector) {
            mJobSelector = selector;
        }

        @Override
        public boolean isInProgress() {
            List<Job> jobs = JobManager.getInstance().findAll(mJobSelector);

            for (Job job : jobs) {
                if (!job.isCancelled() && !job.isFinished()) {
                    return true;
                }
            }

            return false;
        }
    }

    @ViewById(android.R.id.empty)
    TextView mEmptyIndicatorView;

    @ViewById(android.R.id.title)
    TextView mProgressTitleView;

    @ViewById(R.id.progressWheel)
    ProgressWheel mProgressWheel;

    @ViewById(R.id.progressPanel)
    ViewGroup mProgressPanel;

    private boolean mDisplayEmptyIndicatorMessage;
    private ProgressLayoutCallbacks mProgressLayoutCallbacks;

    public ProgressLayout(Context context) {
        super(context);
    }

    public ProgressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProgressLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProgressLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @AfterViews
    void bindViews() {
        setVisibility(View.GONE);
    }

    public void updateProgressView() {
        if (mProgressLayoutCallbacks == null) {
            return;
        }

        if (getVisibility() == View.GONE) {
            setAlpha(0f);
            animate().alpha(1).setDuration(120).start();
            setVisibility(View.VISIBLE);
        }

        if (mProgressLayoutCallbacks.hasContent()) {
            mProgressPanel.setVisibility(View.GONE);
            mEmptyIndicatorView.setVisibility(View.GONE);
            return;
        }

        if (!mProgressLayoutCallbacks.isInProgress()) {
            mProgressPanel.setVisibility(View.GONE);
            if (mDisplayEmptyIndicatorMessage) {
                mEmptyIndicatorView.setVisibility(View.VISIBLE);
            } else {
                mEmptyIndicatorView.setVisibility(View.GONE);
            }
        } else {
            mProgressPanel.setVisibility(View.VISIBLE);
            mEmptyIndicatorView.setVisibility(View.GONE);
        }
    }

    public void setProgressTitle(String text) {
        mProgressTitleView.setText(text);
    }

    public boolean shouldDisplayEmptyIndicatorMessage() {
        return mDisplayEmptyIndicatorMessage;
    }

    public void setShouldDisplayEmptyIndicatorMessage(boolean b) {
        mDisplayEmptyIndicatorMessage = b;
    }

    public ProgressLayoutCallbacks getProgressLayoutCallbacks() {
        return mProgressLayoutCallbacks;
    }

    public void setProgressLayoutCallbacks(ProgressLayoutCallbacks progressLayoutCallbacks) {
        mProgressLayoutCallbacks = progressLayoutCallbacks;
    }
}
