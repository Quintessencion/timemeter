package com.simbirsoft.timemeter.ui.main;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.EventListener;
import com.simbirsoft.timemeter.Consts;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.events.FilterViewStateChangeEvent;
import com.simbirsoft.timemeter.events.ScheduledTaskUpdateTabContentEvent;
import com.simbirsoft.timemeter.injection.ApplicationModule;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.SaveTaskBundleJob;
import com.simbirsoft.timemeter.model.TaskLoadFilter;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.model.TaskChangedEvent;
import com.simbirsoft.timemeter.ui.taskedit.EditTaskFragment;
import com.simbirsoft.timemeter.ui.views.FilterView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.slf4j.Logger;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;


public class MainPageFragment extends BaseFragment {
    public static class SnackbarShowEvent {
        private boolean mVisible;

        public SnackbarShowEvent(boolean visible) {
            mVisible = visible;
        }

        public boolean isVisible() {
            return mVisible;
        }
    }

    public static final int REQEUST_TASK_PROCESSING = 58182253;

    protected Logger LOG = createLogger();

    private static final String SNACKBAR_TAG = "main_page_snackbar";
    private static final String FILTER_STATE = "filter_state";

    private boolean mIsContentInvalidated;
    private boolean mIsSelected;

    private boolean mContentAutoupdateEnabled;

    @Inject
    Bus mBus;

    @Inject
    @Named(ApplicationModule.HANDLER_MAIN)
    Handler mHandler;

    FilterView.FilterState mFilterViewState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injection.sUiComponent.injectMainPageFragment(this);
        restoreInstanceState(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        setContentAutoupdateEnabled(true);
    }

    @Override
    public void onStop() {
        setContentAutoupdateEnabled(false);
        super.onStop();
    }

    private void setContentAutoupdateEnabled(boolean value) {
        if (isSupportAutoupdate()) {
            mContentAutoupdateEnabled = value;
        }
    }

    // Если метод возвращает true, то содержимое страницы будет периодически обновляться.
    // Это необходимо, чтобы пользователь видел актуальную информацию по запущенным задачам.
    protected boolean isSupportAutoupdate() {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        reloadContentIfNeeded();
    }

    protected Bus getBus() {
        return mBus;
    }

    protected FilterView.FilterState getFilterViewState() {
        return mFilterViewState;
    }

    protected boolean hasFilter() {
        return mFilterViewState != null;
    }

    protected boolean filterIsEmpty() {
        return  mFilterViewState == null || mFilterViewState.isEmpty();
    }

    protected void fillTaskLoadFilter(TaskLoadFilter filter) {
        if (mFilterViewState != null) {
            filter.tags(mFilterViewState.tags)
                    .dateMillis(mFilterViewState.dateMillis)
                    .period(mFilterViewState.period)
                    .searchText(mFilterViewState.searchText);
        }
    }

    protected String getFilterLoaderTag(String tag) {
        return tag + "filter:" + String.valueOf(mFilterViewState.hashCode());
    }

    public void onPageSelected() {
        mIsSelected = true;
        reloadContentIfNeeded();
    }

    public void onPageDeselected() {
        mIsSelected = false;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    private void sendTaskChangedEvent(int resultCode, Intent data) {
        mBus.post(new TaskChangedEvent(resultCode, data));
    }

    @Subscribe
    public void onTaskChanged(TaskChangedEvent event) {
        Intent data = event.getData();

        onTaskProcessed(data);

        switch (event.getResultCode()) {
            case EditTaskFragment.RESULT_CODE_CANCELLED:
                onTaskCancelled(data);
                break;
            case EditTaskFragment.RESULT_CODE_TASK_CREATED:
                onTaskCreated(data);
                break;
            case EditTaskFragment.RESULT_CODE_TASK_REMOVED:
                onTaskRemoved(data);
                break;
            case EditTaskFragment.RESULT_CODE_TASK_UPDATED:
                onTaskUpdated(data);
                break;
            case EditTaskFragment.RESULT_CODE_TASK_RECREATED:
                onTaskRecreated(data);
                break;
        }
    }

    @Subscribe
    public void onFilterViewStateChanged(FilterViewStateChangeEvent ev) {
        if (!isAdded()) {
            return;
        }
        mFilterViewState = ev.getFilterState();
        onFilterViewStateChanged();
    }

    protected void onFilterViewStateChanged() {

    }

    @Subscribe
    public void onUpdateTabContent(ScheduledTaskUpdateTabContentEvent ev) {
        if (mContentAutoupdateEnabled) {
            invalidateContent();
            reloadContentIfNeeded();
        }
    }

    @OnJobSuccess(SaveTaskBundleJob.class)
    public void onTaskSaved() {
        mBus.post(new TaskChangedEvent(EditTaskFragment.RESULT_CODE_TASK_RECREATED, null));
    }

    @OnJobFailure(SaveTaskBundleJob.class)
    public void onTaskSaveFailed() {
        Snackbar bar = Snackbar.with(getActivity())
                .text(R.string.error_unable_to_backup_task)
                .colorResource(R.color.lightRed)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE);
        if (getRecyclerView() != null) {
            bar.attachToRecyclerView(getRecyclerView());
        }
        bar.setTag(SNACKBAR_TAG);
        SnackbarManager.show(bar);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (mFilterViewState != null) {
            bundle.putParcelable(FILTER_STATE, mFilterViewState);
        }
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mFilterViewState = savedInstanceState.getParcelable(FILTER_STATE);
        }
    }

    protected void showTaskRemoveUndoBar(TaskBundle bundle) {
        mBus.post(new SnackbarShowEvent(true));
        final Snackbar bar = Snackbar.with(getActivity())
                .type(SnackbarType.MULTI_LINE)
                .actionLabel(R.string.action_undo_remove)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                .color(getResources().getColor(R.color.primaryDark))
                .actionListener((snackbar) -> backupRemovedTask(bundle, snackbar))
                .animation(true)
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
                        mBus.post(new SnackbarShowEvent(false));
                    }
                });
        if (getRecyclerView() != null) {
            bar.attachToRecyclerView(getRecyclerView());
        }

        new AsyncTask<Void, Void, CharSequence>() {
            @Override
            protected CharSequence doInBackground(Void... voids) {
                final String description;

                if (bundle.hasPersistedState()) {
                    description = bundle.createOriginalBundle().getTask().getDescription();
                } else {
                    description = bundle.getTask().getDescription();
                }

                final String undoMessage = getString(R.string.hint_task_removed)
                        + "\n"
                        + description;

                final SpannableStringBuilder sb = new SpannableStringBuilder(undoMessage);
                final StyleSpan iss = new StyleSpan(android.graphics.Typeface.ITALIC);
                sb.setSpan(iss, undoMessage.length() - description.length(),
                        undoMessage.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                return sb;
            }

            @Override
            protected void onPostExecute(CharSequence text) {
                if (isAdded() && !getActivity().isFinishing()) {
                    bar.text(text);
                    SnackbarManager.show(bar);
                }
            }
        }.execute();
    }

    private void backupRemovedTask(TaskBundle taskBundle, Snackbar snackbar) {
        long delay = 0;
        if (snackbar != null) {
            delay = Consts.DISMISS_DELAY_MILLIS;
            snackbar.dismiss();
        }

        final AsyncTask unmarshallTask = new AsyncTask<Void, Void, TaskBundle>() {
            @Override
            protected TaskBundle doInBackground(Void... voids) {
                return taskBundle.createOriginalBundle();
            }
        }.execute();

        mHandler.postDelayed(() -> {
            try {
                SaveTaskBundleJob job = Injection.sJobsComponent.saveTaskBundleJob();
                job.setTaskBundle((TaskBundle) unmarshallTask.get());
                submitJob(job);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, delay);
    }


    protected RecyclerView getRecyclerView() {
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQEUST_TASK_PROCESSING) {
            sendTaskChangedEvent(resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void onTaskProcessed(Intent data) {

    }

    protected void onTaskCancelled(Intent data) {
        LOG.debug("result: task edit cancelled");
    }

    protected void onTaskCreated(Intent data) {
        LOG.debug("result: task created");
    }

    protected void onTaskUpdated(Intent data) {
        LOG.debug("result: task updated");
    }

    protected void onTaskRemoved(Intent data) {
        LOG.debug("result: task removed");
    }

    protected void onTaskRecreated(Intent data) {
        LOG.debug("result: task recreated");

        invalidateContent();
        reloadContentIfNeeded();
    }

    private void reloadContentIfNeeded() {
        if (isSelected() && mIsContentInvalidated) {
            reloadContent();
        }
    }

    protected void reloadContent() {
        mIsContentInvalidated = false;
    }

    protected void invalidateContent() {
        mIsContentInvalidated = true;
    }

    protected Logger createLogger() {
        return null;
    }
}
