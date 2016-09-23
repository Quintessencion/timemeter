package com.simbirsoft.timeactivity.ui.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobLoaderManager;
import com.be.android.library.worker.handlers.JobEventDispatcher;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.EventListener;
import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.db.model.TaskTimeSpan;
import com.simbirsoft.timeactivity.injection.Injection;
import com.simbirsoft.timeactivity.jobs.RemoveTaskTimeSpanJob;
import com.simbirsoft.timeactivity.jobs.RestoreTaskTimeSpansJob;

import java.util.List;

public class TaskTimeSpanActions implements ActionMode.Callback, JobLoader.JobLoaderCallbacks {

    private static final String REMOVE_SPAN_JOB = "remove_span_job";
    private static final String RESTORE_SPAN_JOB = "restore_span_job";

    public interface OnActionListener {
        void onAction(TaskTimeSpanActions sender);
    }

    private FragmentActivity mActivityContext;
    private TaskActivitiesAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ActionMode mActionMode;
    private DataObserver mDataObserver;
    private JobEventDispatcher mEventDispatcher;

    private Bundle mActivitiesBackup;

    private OnActionListener mOnEditListener;
    private OnActionListener mOnRemoveListener;
    private OnActionListener mOnActivated;
    private OnActionListener mOnDeactivated;
    private OnActionListener mOnDidRemoveListener;
    private OnActionListener mOnDidRestoreSpansListener;

    public TaskTimeSpanActions(FragmentActivity activityContext, Bundle savedState) {
        mActivityContext = activityContext;
        restoreState(savedState);
    }

    public void bind(TaskActivitiesAdapter adapter, RecyclerView recyclerView) {
        mAdapter = adapter;
        mRecyclerView = recyclerView;
        mDataObserver = new DataObserver();
        mAdapter.registerAdapterDataObserver(mDataObserver);
    }

    public void setOnEditListener(OnActionListener onEditListener) {
        mOnEditListener = onEditListener;
    }

    public void setOnRemoveListener(OnActionListener onRemoveListener) {
        mOnRemoveListener = onRemoveListener;
    }

    public void setOnActivatedListener(OnActionListener listener) {
        mOnActivated = listener;
    }

    public void setOnDeactivatedListener(OnActionListener listener) {
        mOnDeactivated = listener;
    }

    public void setOnDidRemoveListener(OnActionListener listener) {
        mOnDidRemoveListener = listener;
    }

    public void setOnDidRestoreSpansListener(OnActionListener listener) {
        mOnDidRestoreSpansListener = listener;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.task_activities_context_menu, menu);
        updateActionBarMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                fireActionCallback(mOnEditListener);
                return true;

            case R.id.remove:
                fireActionCallback(mOnRemoveListener);
                removeSelectedSpans();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        mAdapter.clearSelection();
        fireActionCallback(mOnDeactivated);
    }

    private void updateActionBarMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.edit);
        if (item == null) {
            return;
        }
        item.setVisible(mAdapter.getSelectedSpans().size() == 1);
    }

    private void fireActionCallback(OnActionListener listener) {
        if (listener != null) {
            listener.onAction(this);
        }
    }

    private void updateActionBar() {
        List<TaskTimeSpan> selected = mAdapter.getSelectedSpans();
        if (selected.isEmpty()) {
            if (mActionMode != null) {
                mActionMode.finish();
            }
        } else {
            if(mActionMode == null) {
                mActionMode = mActivityContext.startActionMode(this);
                fireActionCallback(mOnActivated);
            } else {
                updateActionBarMenu(mActionMode.getMenu());
            }
        }
    }

    private class DataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            updateActionBar();
        }
    }

    @Override
    public Job onCreateJob(String s) {
        if (REMOVE_SPAN_JOB.equals(s)) {
            RemoveTaskTimeSpanJob job = Injection.sJobsComponent.removeTaskTimeSpanJob();
            job.setSpan(mAdapter.getSelectedSpanIds());
            return job;
        }
        if (RESTORE_SPAN_JOB.equals(s)) {
            RestoreTaskTimeSpansJob job = Injection.sJobsComponent.restoreTaskTimeSpanJob();
            job.setBackupBundle(mActivitiesBackup);
            mActivitiesBackup = null;
            return job;
        }
        throw new UnsupportedOperationException("Unknown job id");
    }

    @OnJobSuccess(RemoveTaskTimeSpanJob.class)
    public void onRemoveSuccess(LoadJobResult<Bundle> result) {
        mActivitiesBackup = result.getData();
        showUndoRemoveSnackbar();
        fireActionCallback(mOnDidRemoveListener);
    }

    @OnJobSuccess(RestoreTaskTimeSpansJob.class)
    public void onRestoreSuccess(JobEvent event) {
        fireActionCallback(mOnDidRestoreSpansListener);
    }

    private void removeSelectedSpans() {
        requestLoad(REMOVE_SPAN_JOB, this);
    }

    public void showUndoRemoveSnackbar() {
        Snackbar sb = Snackbar.with(mActivityContext)
                .type(SnackbarType.MULTI_LINE)
                .text(R.string.hint_task_time_spans_removed)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                .color(mActivityContext.getResources().getColor(R.color.primaryDark))
                .actionLabel(R.string.action_undo_remove)
                .actionListener(v -> requestLoad(RESTORE_SPAN_JOB, this))
                .eventListener(new EventListener() {
                    @Override
                    public void onShow(Snackbar snackbar) {

                    }

                    @Override
                    public void onShown(Snackbar snackbar) {

                    }

                    @Override
                    public void onDismiss(Snackbar snackbar) {

                    }

                    @Override
                    public void onDismissed(Snackbar snackbar) {
                        mActivitiesBackup = null;
                    }
                })
                .animation(true)
                .attachToRecyclerView(mRecyclerView);
        SnackbarManager.show(sb);
    }

    private void restoreState(Bundle savedInstanceState) {
        String listenerName = getClass().getName() + "event_dispatcher";
        mEventDispatcher = new JobEventDispatcher(mActivityContext, listenerName);
        mEventDispatcher.restoreState(savedInstanceState);
        mEventDispatcher.register(this);
    }

    public void saveState(Bundle outState) {
        mEventDispatcher.saveState(outState);
    }

    public void dispose() {
        mEventDispatcher.unregister(this);
    }

    private int requestLoad(String loaderAttachTag, JobLoader.JobLoaderCallbacks callbacks) {
        JobLoaderManager mgr = JobLoaderManager.getInstance();
        JobLoader loader = mgr.initLoader(mEventDispatcher, loaderAttachTag, callbacks);

        return loader.requestLoad();
    }
}
