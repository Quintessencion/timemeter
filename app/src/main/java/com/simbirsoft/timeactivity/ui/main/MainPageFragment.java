package com.simbirsoft.timeactivity.ui.main;

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
import com.simbirsoft.timeactivity.Consts;
import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.controller.HelpCardController;
import com.simbirsoft.timeactivity.db.Preferences;
import com.simbirsoft.timeactivity.events.FilterViewStateChangeEvent;
import com.simbirsoft.timeactivity.events.HelpCardPresentedEvent;
import com.simbirsoft.timeactivity.events.ReadyToShowHelpCardEvent;
import com.simbirsoft.timeactivity.events.ScheduledTaskUpdateTabContentEvent;
import com.simbirsoft.timeactivity.injection.ApplicationModule;
import com.simbirsoft.timeactivity.injection.Injection;
import com.simbirsoft.timeactivity.jobs.SaveTaskBundleJob;
import com.simbirsoft.timeactivity.model.TaskLoadFilter;
import com.simbirsoft.timeactivity.ui.base.BaseFragment;
import com.simbirsoft.timeactivity.ui.helpcards.HelpCard;
import com.simbirsoft.timeactivity.ui.helpcards.HelpCardDataSource;
import com.simbirsoft.timeactivity.ui.helpcards.HelpCardPresenter;
import com.simbirsoft.timeactivity.ui.helpcards.HelpCardSource;
import com.simbirsoft.timeactivity.ui.model.TaskBundle;
import com.simbirsoft.timeactivity.ui.model.TaskChangedEvent;
import com.simbirsoft.timeactivity.ui.taskedit.EditTaskFragment;
import com.simbirsoft.timeactivity.ui.views.FilterView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.EFragment;
import org.slf4j.Logger;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;


@EFragment
public class MainPageFragment extends BaseFragment implements HelpCardSource {

    public static class SnackbarShowEvent {
        private boolean mVisible;

        public SnackbarShowEvent(boolean visible) {
            mVisible = visible;
        }

        public boolean isVisible() {
            return mVisible;
        }
    }

    public static final int REQUEST_CODE_PROCESS_TASK = 14688;

    protected Logger mLogger = createLogger();

    private static final String SNACKBAR_TAG = "main_page_snackbar";

    private boolean mIsContentInvalidated;
    private boolean mIsSelected;

    private boolean mContentAutoupdateEnabled;

    @Inject
    Bus mBus;

    @Inject
    @Named(ApplicationModule.HANDLER_MAIN)
    Handler mHandler;

    @Inject
    HelpCardController mHelpCardController;

    @Inject
    Preferences mPrefs;

    private FilterViewResultsProvider mFilterViewResultsProvider;
    private int mCurrentHelpCardId = HelpCardController.HELP_CARD_NONE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injection.sUiComponent.injectMainPageFragment(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        setContentAutoupdateEnabled(true);
        presentHelpCardIfAny();
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

    protected int getHelpCardToPresent(HelpCardController controller) {
        return HelpCardController.HELP_CARD_NONE;
    }

    protected void presentHelpCardIfAny() {
        if (!mPrefs.getIsShowHelp()) {
            return;
        }

        if (getHelpCardPresenter() == null || !mPrefs.getUserLearnedDrawer())
            return;

        int oldId = mCurrentHelpCardId;
        mCurrentHelpCardId = getHelpCardToPresent(mHelpCardController);

        if (mCurrentHelpCardId == oldId) {
            return;
        }

        if (mCurrentHelpCardId != HelpCardController.HELP_CARD_NONE) {
            getHelpCardPresenter().show();
        } else {
            getHelpCardPresenter().hide();
        }
    }

    protected HelpCardPresenter getHelpCardPresenter() {
        return null;
    }

    protected Bus getBus() {
        return mBus;
    }

    protected FilterView.FilterState getFilterViewState() {
        if (mFilterViewResultsProvider == null)
            return null;

        FilterView filterView = mFilterViewResultsProvider.getFilterView();
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

    protected void setContentInvalidated(boolean isContentInvalidated) {
        mIsContentInvalidated = isContentInvalidated;
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
        if (isAdded()) {
            onFilterViewStateChanged();
        }
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
        if (requestCode == REQUEST_CODE_PROCESS_TASK) {
            invalidateContent();
            sendTaskChangedEvent(resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void onTaskProcessed(Intent data) {

    }

    protected void onTaskCancelled(Intent data) {
        mLogger.debug("result: task edit cancelled");
    }

    protected void onTaskCreated(Intent data) {
        mLogger.debug("result: task created");
    }

    protected void onTaskUpdated(Intent data) {
        mLogger.debug("result: task updated");
    }

    protected void onTaskRemoved(Intent data) {
        mLogger.debug("result: task removed");
    }

    protected void onTaskRecreated(Intent data) {
        mLogger.debug("result: task recreated");

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

    public void setFilterViewResultsProvider(FilterViewResultsProvider provider) {
        mFilterViewResultsProvider = provider;
    }

    protected void updateFilterResultsView(int taskCount, FilterView.FilterState filterState) {
        mFilterViewResultsProvider.updateFilterResultsView(taskCount, filterState);
    }

    protected void onHelpCardNextClicked(HelpCard sender, int cardID) {
        if (sender.isLastItemPresented()) {
            mHelpCardController.markPresented(cardID);
        }
    }

    protected void onHelpCardActionClicked(HelpCard sender, int cardID) {

    }

    protected void onHelpCardClicked(HelpCard sender, int cardID) {

    }

    @Subscribe
    public void onHelpCardPresentedEvent(HelpCardPresentedEvent ev) {
        presentHelpCardIfAny();
    }

    @Override
    public void setupHelpCard(HelpCard helpCard) {
        helpCard.setOnNextClickListener(v -> {
            onHelpCardNextClicked(helpCard, mCurrentHelpCardId);
        });
        helpCard.setOnActionClickListener(v -> {
            onHelpCardActionClicked(helpCard, mCurrentHelpCardId);
        });
        helpCard.setOnClickListener(v -> {
            onHelpCardClicked(helpCard, mCurrentHelpCardId);
        });

        final HelpCardDataSource ds = mHelpCardController.getCard(mCurrentHelpCardId);
        helpCard.setDataSource(ds);
    }

    @Override
    public int getHelpCardId() {
        return mCurrentHelpCardId;
    }

    @Subscribe
    public void onReadyToShowHelpCardEvent(ReadyToShowHelpCardEvent ev) {
        presentHelpCardIfAny();
    }

    public FilterViewProvider getFilterViewProvider() {
        return mFilterViewResultsProvider;
    }
}
