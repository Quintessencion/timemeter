package com.simbirsoft.timemeter.ui.main;

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

    private static final String SNACKBAR_TAG = "main_page_snackbar";

    private boolean mIsContentInvalidated;
    private boolean mIsSelected;

    @Inject
    Bus mBus;

    @Inject
    @Named(ApplicationModule.HANDLER_MAIN)
    Handler mHandler;

    FilterViewProvider mFilterViewProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injection.sUiComponent.injectMainPageFragment(this);
    }

    protected Bus getBus() {
        return mBus;
    }

    protected FilterView.FilterState getFilterViewState() {
        if (mFilterViewProvider == null)
            return null;

        FilterView filterView = mFilterViewProvider.getFilterView();
        if (filterView == null)
            return null;

        return filterView.getViewFilterState();
    }

    protected boolean hasFilter() {
        return getFilterViewState() != null;
    }

    protected boolean filterIsEmpty() {
        FilterView.FilterState filterState = getFilterViewState();
        return  filterState == null || filterState.isEmpty();
    }

    protected void fillTaskLoadFilter(TaskLoadFilter filter) {
        FilterView.FilterState filterState = getFilterViewState();
        if (filterState != null) {
            filter.tags(filterState.tags)
                    .startDateMillis(filterState.dateMillis)
                    .period(filterState.period)
                    .searchText(filterState.searchText);
        }
    }

    protected String getFilterLoaderTag(String tag) {
        return tag + "filter:" + String.valueOf(getFilterViewState().hashCode());
    }

    public void onPageSelected() {
        mIsSelected = true;
        if (mIsContentInvalidated) {
            mIsContentInvalidated = false;
            reloadContent();
        }
    }

    public void onPageDeselected() {
        mIsSelected = false;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    protected void sendTaskChangedEvent(int resultCode) {
        mBus.post(new TaskChangedEvent(resultCode));
    }

    protected void setContentInvalidated(boolean isContentInvalidated) {
        mIsContentInvalidated = isContentInvalidated;
    }

    @Subscribe
    public void onTaskChanged(TaskChangedEvent event) {
        if (mIsSelected && event.getResultCode() == EditTaskFragment.RESULT_CODE_TASK_RECREATED) {
            reloadContent();
            return;
        }
        if (!mIsSelected && (event.getResultCode() == EditTaskFragment.RESULT_CODE_TASK_RECREATED
                                ||needUpdateAfterTaskChanged(event.getResultCode()))) {
            mIsContentInvalidated = true;
        }
    }

    @Subscribe
    public void onFilterViewStateChanged(FilterViewStateChangeEvent ev) {
        if (isAdded()) {
            onFilterViewStateChanged();
        }
    }

    protected void onFilterViewStateChanged() {

    }

    protected boolean needUpdateAfterTaskChanged(int resultCode) {
        return true;
    }

    @OnJobSuccess(SaveTaskBundleJob.class)
    public void onTaskSaved() {
        if (mIsSelected) {
            reloadContent();
        }
        mBus.post(new TaskChangedEvent(EditTaskFragment.RESULT_CODE_TASK_RECREATED));
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

    protected void reloadContent() {

    }

    public void setFilterViewProvider(FilterViewProvider provider) {
        mFilterViewProvider = provider;
    }
}
