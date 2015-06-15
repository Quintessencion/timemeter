package com.simbirsoft.timemeter.ui.tasklist;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.text.Spanned;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.be.android.library.worker.util.JobSelector;
import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.simbirsoft.timemeter.Consts;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.controller.ActiveTaskInfo;
import com.simbirsoft.timemeter.controller.HelpCardController;
import com.simbirsoft.timemeter.controller.ITaskActivityManager;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.Preferences;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.events.ScheduledTaskUpdateTabContentEvent;
import com.simbirsoft.timemeter.events.TaskActivityStoppedEvent;
import com.simbirsoft.timemeter.events.TaskActivityUpdateEvent;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTaskListJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.AppAlertDialogFragment;
import com.simbirsoft.timemeter.ui.base.DialogContainerActivity;
import com.simbirsoft.timemeter.ui.helpcards.HelpCardAdapter;
import com.simbirsoft.timemeter.ui.helpcards.HelpCardAnimator;
import com.simbirsoft.timemeter.ui.helpcards.HelpCard;
import com.simbirsoft.timemeter.ui.base.FragmentContainerActivity;
import com.simbirsoft.timemeter.ui.main.ContentFragmentCallbacks;
import com.simbirsoft.timemeter.ui.main.MainPageFragment;
import com.simbirsoft.timemeter.ui.main.MainPagerAdapter;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.settings.SettingsActivity;
import com.simbirsoft.timemeter.ui.taskedit.EditTaskFragment;
import com.simbirsoft.timemeter.ui.taskedit.EditTaskFragment_;
import com.simbirsoft.timemeter.ui.taskedit.ViewTaskFragment;
import com.simbirsoft.timemeter.ui.taskedit.ViewTaskFragment_;
import com.simbirsoft.timemeter.ui.util.TaskFilterPredicate;
import com.simbirsoft.timemeter.ui.helpcards.HelpCardPresenter;
import com.simbirsoft.timemeter.ui.views.FilterView;
import com.simbirsoft.timemeter.ui.views.TagView;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;

import javax.inject.Inject;

@EFragment(R.layout.fragment_task_list)
public class TaskListFragment extends MainPageFragment implements JobLoader.JobLoaderCallbacks,
        TaskListAdapter.TaskClickListener,
        MainPagerAdapter.PageTitleProvider {

    private static final Logger LOG = LogFactory.getLogger(TaskListFragment.class);

    private static final String SNACKBAR_TAG = "task_list_snackbar";
    private static final String TASK_LIST_LOADER_TAG = "TaskListFragment_";
    private static final int COLUMN_COUNT_DEFAULT = 2;
    private static final int LOAD_TASK_JOB_ID = 2970017;
    private static final int REQUEST_CODE_DELETE_TEST_DATA = 10001;

    private int mColumnCount;

    @ViewById(android.R.id.list)
    RecyclerView mRecyclerView;

    @ViewById(R.id.taskListContentRoot)
    ViewGroup mTaskListContentRoot;

    @ViewById(android.R.id.empty)
    TextView mEmptyListIndicator;

    @InstanceState
    int[] mTaskListPosition;

    @Inject
    DatabaseHelper mDatabaseHelper;

    Preferences mPrefs;

    boolean mDataIsLoaded;

    private FloatingActionButton mFloatingActionButton;
    private TaskListAdapter mTasksViewAdapter;
    private HelpCardAdapter mHelpCardAdapter;
    private ITaskActivityManager mTaskActivityManager;
    private ContentFragmentCallbacks mCallbacks;

    private final TagView.TagViewClickListener mTagViewClickListener = tagView -> {
        FilterView filterView = getFilterViewProvider().getFilterView();
        if (tagView.isChecked()) {
            filterView.getTagsView().removeObject(tagView.getTag());
        } else {
            filterView.getTagsView().addObject(tagView.getTag());
        }
        mTasksViewAdapter.setTagListForChecking(filterView.getTagsView().getObjects());
    };

    private void onFloatingButtonClicked(View v) {
        mLogger.info("floating button clicked");

        Snackbar current = SnackbarManager.getCurrentSnackbar();
        long delay = 0;
        if (current != null && current.isShowing()) {
            delay = Consts.DISMISS_DELAY_MILLIS;
            current.dismiss();
        }

        mRecyclerView.postDelayed(() -> {
            Bundle args = new Bundle();
            args.putString(EditTaskFragment.EXTRA_TITLE, getString(R.string.title_begin_new_task));

            Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                    getActivity(), EditTaskFragment_.class.getName(), args);
            getActivity().startActivityForResult(launchIntent, MainPageFragment.REQUEST_CODE_PROCESS_TASK);
        }, delay);
    }

    private boolean onFloatingActionButtonLongClicked(View v) {
        showToast(R.string.hint_new_task);
        return true;
    }

    @AfterViews
    void bindViews() {
        final ViewGroup floatingButtonContainer = (ViewGroup) LayoutInflater.from(getActivity())
                .inflate(R.layout.view_floating_action_button, mTaskListContentRoot, false);
        mFloatingActionButton = (FloatingActionButton) floatingButtonContainer.findViewById(R.id.floatingButton);
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mTaskListContentRoot.addView(floatingButtonContainer, params);
        mFloatingActionButton.attachToRecyclerView(mRecyclerView);
        mFloatingActionButton.setOnClickListener(this::onFloatingButtonClicked);
        mFloatingActionButton.setOnLongClickListener(this::onFloatingActionButtonLongClicked);
        mRecyclerView.setHasFixedSize(false);

        mEmptyListIndicator.setVisibility(View.GONE);

        mColumnCount = COLUMN_COUNT_DEFAULT;
        if (!getResources().getBoolean(R.bool.isTablet)) {
            mColumnCount = 1;
        }

        StaggeredGridLayoutManager tasksViewLayoutManager = new StaggeredGridLayoutManager(
                mColumnCount,
                StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(tasksViewLayoutManager);

        mTasksViewAdapter = new TaskListAdapter(mTaskActivityManager);
        mTasksViewAdapter.setTaskClickListener(this);
        mTasksViewAdapter.setTagViewClickListener(mTagViewClickListener);

        mHelpCardAdapter = new HelpCardAdapter(mTasksViewAdapter);
        mHelpCardAdapter.setHelpCardSource(this);
        mHelpCardAdapter.setLayoutManager(tasksViewLayoutManager);
        mRecyclerView.setAdapter(mHelpCardAdapter);
        mRecyclerView.setItemAnimator(new HelpCardAnimator());

        requestLoad(TASK_LIST_LOADER_TAG, this);
        applyFilterState();
    }

    private void applyFilterState() {
        FilterView filterView = getFilterViewProvider().getFilterView();
        mTasksViewAdapter.setTagListForChecking(filterView.getTagsView().getObjects());
        //filterView.postFilterUpdate();
    }

    @Override
    public void onDestroyView() {
        RelativeLayout containerRoot = mCallbacks.getContainerUnderlayView();
        containerRoot.removeView((View) mFloatingActionButton.getParent());

        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallbacks = (ContentFragmentCallbacks) activity;
    }

    @Override
    public void onDetach() {
        mCallbacks = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Injection.sUiComponent.injectTaskListFragment(this);

        mPrefs = Injection.sDatabaseComponent.preferences();

        mTaskActivityManager = Injection.sTaskManager.taskActivityManager();
        getBus().register(this);
    }

    @Override
    public void onDestroy() {
        getBus().unregister(this);

        if (mTasksViewAdapter != null) {
            mTasksViewAdapter.setTaskClickListener(null);
        }

        Snackbar snackbar = SnackbarManager.getCurrentSnackbar();
        if (snackbar != null && snackbar.isShowing()) {
            snackbar.dismiss();
        }

        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();

        mTaskListPosition = new int[mColumnCount];

        if (mTasksViewAdapter.getItemCount() > 0) {
            mTaskListPosition = ((StaggeredGridLayoutManager)
                    mRecyclerView.getLayoutManager()).findFirstVisibleItemPositions(mTaskListPosition);
        }

        mTaskActivityManager.saveTaskActivity();
    }

    private void removeTaskFromList(long taskId) {
        if (mTasksViewAdapter == null) return;

        mTasksViewAdapter.removeItems(taskId);
    }

    private void addTaskToList(TaskBundle task) {
        if (mTasksViewAdapter == null) return;

        if (hasFilter()) {
            TaskFilterPredicate predicate = new TaskFilterPredicate(getFilterViewState());
            if (!predicate.apply(task)) {
                mLogger.debug("created task isn't match current filter");
                return;
            }
        }

        mTasksViewAdapter.addFirstItem(task);
    }

    private void replaceTaskInList(TaskBundle task) {
        if (mTasksViewAdapter == null) return;

        mTasksViewAdapter.replaceItem(task);
    }

    private void showTaskAddedBar(TaskBundle bundle) {
        final String message = String.format(getString(R.string.action_task_added),
                                             "<i>" + bundle.getTask().getDescription() + "</i>");
        final Spanned messageSpanned = Html.fromHtml(message);
        Snackbar bar = Snackbar.with(getActivity())
                .text(messageSpanned)
                .colorResource(R.color.blue)
                .attachToRecyclerView(mRecyclerView);
        bar.setTag(SNACKBAR_TAG);
        SnackbarManager.show(bar);
    }

    @OnJobSuccess(LoadTaskListJob.class)
    public void onTaskListLoaded(LoadJobResult<List<TaskBundle>> event) {
        mTasksViewAdapter.setItems(event.getData());

        updateFilterResultsView(mTasksViewAdapter.getItemCount(), getFilterViewState());

        if (mTaskListPosition != null) {
            mRecyclerView.getLayoutManager().scrollToPosition(mTaskListPosition[0]);
            mTaskListPosition = null;
        }

        updateEmptyListIndicator();

        mDataIsLoaded = true;

        presentHelpCardIfAny();
    }

    private void updateEmptyListIndicator() {
        if (filterIsEmpty() && mHelpCardAdapter.getItemCount() == 0) {
            mEmptyListIndicator.setVisibility(View.VISIBLE);
        } else {
            mEmptyListIndicator.setVisibility(View.GONE);
        }
    }

    @OnJobFailure(LoadTaskListJob.class)
    public void onTaskListLoadFailed() {
        showToast(R.string.error_unable_to_load_task_list);
    }

    @Override
    public Job onCreateJob(String loaderAttachTag) {
        LoadTaskListJob job = Injection.sJobsComponent.loadTaskListJob();
        job.setGroupId(LOAD_TASK_JOB_ID);

        fillTaskLoadFilter(job.getTaskLoadFilter());
        job.addTag(TASK_LIST_LOADER_TAG);

        return job;
    }

    @Override
    public void onTaskCardClicked(TaskBundle item) {
        mLogger.info("task card clicked; task {}", item);

        final Task task = item.getTask();

        if (mTaskActivityManager.hasActiveTask()) {
            if (!mTaskActivityManager.isTaskActive(item.getTask())) {
                Task currentTask = mTaskActivityManager.getActiveTaskInfo().getTask();
                mTaskActivityManager.stopTask(currentTask);
                mTasksViewAdapter.updateItemView(mRecyclerView, currentTask);
            }
        }

        if (mTaskActivityManager.isTaskActive(task)) {
            mTaskActivityManager.stopTask(task);
        } else {
            mTaskActivityManager.startTask(task);
        }
        mTasksViewAdapter.updateItemView(mRecyclerView, task);
    }

    @Override
    public void onTaskViewClicked(TaskBundle item) {
        mLogger.debug("view task: {}", item);

        SnackbarManager.dismiss();

        Bundle args = new Bundle();
        args.putParcelable(ViewTaskFragment.EXTRA_TASK_BUNDLE, item);

        Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                getActivity(), ViewTaskFragment_.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, MainPageFragment.REQUEST_CODE_PROCESS_TASK);
    }

    @Override
    public void onTaskViewLongClicked(TaskBundle item, View itemView) {
        showToastWithAnchor(R.string.hint_view_task, itemView);
    }

    @Override
    protected void onFilterViewStateChanged() {
        JobManager.getInstance().cancelAll(JobSelector.forJobTags(TASK_LIST_LOADER_TAG));

        String loaderTag = getFilterLoaderTag(TASK_LIST_LOADER_TAG);
        requestLoad(loaderTag, this);

    }

    @Subscribe
    public void onTaskActivityUpdate(TaskActivityUpdateEvent event) {
        ActiveTaskInfo info = event.getActiveTaskInfo();
        if (mRecyclerView != null && mTasksViewAdapter != null) {
            mTasksViewAdapter.updateItemView(mRecyclerView, info.getTask());
        }
    }

    @Subscribe
    public void onTaskActivityStopped(TaskActivityStoppedEvent event) {
        if (mTasksViewAdapter != null) {
            mTasksViewAdapter.updateItemView(mRecyclerView, event.task);
        }
    }

    @Override
    public String getPageTitle(Resources resources) {
        return resources.getString(R.string.title_tasks);
    }

    @Subscribe
    public void onSnackbarVisibilityChanged(SnackbarShowEvent event) {
        if (mFloatingActionButton == null) return;
        if (event.isVisible()) {
            mFloatingActionButton.hide(false);
        } else {
            mFloatingActionButton.attachToRecyclerView(mRecyclerView);
            mFloatingActionButton.show(true);
        }
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    protected void reloadContent() {
        super.reloadContent();
        requestReload(TASK_LIST_LOADER_TAG, this);
    }

    @Override
    protected boolean isSupportAutoupdate() {
        return false;
    }

    private TaskBundle getTaskBundle(Intent data) {
        return data.getParcelableExtra(EditTaskFragment.EXTRA_TASK_BUNDLE);
    }

    @Override
    protected void onTaskCreated(Intent data) {
        super.onTaskCreated(data);

        TaskBundle bundle = getTaskBundle(data);
        addTaskToList(bundle);
        showTaskAddedBar(bundle);

        presentHelpCardIfAny();
    }

    @Override
    protected void onTaskUpdated(Intent data) {
        super.onTaskUpdated(data);

        TaskBundle bundle = getTaskBundle(data);
        replaceTaskInList(bundle);
    }

    @Override
    protected void onTaskRemoved(Intent data) {
        super.onTaskRemoved(data);

        TaskBundle bundle = getTaskBundle(data);
        removeTaskFromList(bundle.getTask().getId());
        showTaskRemoveUndoBar(bundle);

        invalidateContent();

        presentHelpCardIfAny();
    }

    @Override
    protected Logger createLogger() {
        return LogFactory.getLogger(TaskListFragment.class);
    }

    @Override
    public void onPageSelected() {
        super.onPageSelected();
        if (mTaskActivityManager != null && mTaskActivityManager.hasActiveTask()) {
            final ActiveTaskInfo activeTaskInfo = mTaskActivityManager.getActiveTaskInfo();
            if (activeTaskInfo != null) {
                final int position = mTasksViewAdapter.findTaskPosition(activeTaskInfo.getTask());
                if (position >= 0) {
                    RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
                    layoutManager.scrollToPosition(position);
                }
            }
        }
    }
            
    protected int getHelpCardToPresent(HelpCardController controller) {
        if (!controller.isPresented(HelpCardController.HELP_CARD_TASK_LIST)) {
            return HelpCardController.HELP_CARD_TASK_LIST;
        }

        if (mDataIsLoaded && mTasksViewAdapter.getItemCount() == 0 && !controller.isPresented(HelpCardController.HELP_CARD_ADD_NEW_TASK)) {
            return HelpCardController.HELP_CARD_ADD_NEW_TASK;
        }

        boolean basicCardsPresented = controller.isPresented(
                HelpCardController.HELP_CARD_TASK_LIST,
                HelpCardController.HELP_CARD_STATS_LIST,
                HelpCardController.HELP_CARD_CALENDAR);

        boolean demosExist = mDatabaseHelper.isDemoDatasExist();

        boolean demoDatasPresented = controller.isPresented(HelpCardController.HELP_CARD_DEMO_DATAS);

        if (basicCardsPresented && demosExist && !demoDatasPresented) {
            return HelpCardController.HELP_CARD_DEMO_DATAS;
        }

        return super.getHelpCardToPresent(controller);
    }

    @Override
    protected void onHelpCardActionClicked(HelpCard sender, int helpCardID) {
        if (helpCardID == HelpCardController.HELP_CARD_DEMO_DATAS) {
            showDeleteTestDataDialog();
        }
    }

    @Override
    protected void onHelpCardClicked(HelpCard sender, int helpCardID) {
        if (helpCardID == HelpCardController.HELP_CARD_ADD_NEW_TASK) {
            onFloatingButtonClicked(null);
        }
    }

    @Override
    protected HelpCardPresenter getHelpCardPresenter() {
        return mHelpCardAdapter;
    }

    @Override
    protected void presentHelpCardIfAny() {
        super.presentHelpCardIfAny();
        updateEmptyListIndicator();
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

    @OnActivityResult(REQUEST_CODE_DELETE_TEST_DATA)
    public void onDeleteTestDataDialogResult(int resultCode, Intent data) {
        if (resultCode == AppAlertDialogFragment.RESULT_CODE_ACCEPTED) {
            deleteTestData();
        }
    }

    @OnActivityResult(SettingsActivity.REQUEST_CODE_PREFERENCE_SCREEN)
    public void onPreferenceScreenClosed(int resultCode, Intent data) {
        if (resultCode == SettingsActivity.RESULT_CODE_PREFERENCES_MODIFIED) {
            reloadContent();
        }

    }

    private void deleteTestData() {
        mDatabaseHelper.removeTestData();
        reloadContent();
        mPrefs.setIsDemoTasksDeleted(false);
        // force update for other tabs
        getBus().post(new ScheduledTaskUpdateTabContentEvent());
    }
}

