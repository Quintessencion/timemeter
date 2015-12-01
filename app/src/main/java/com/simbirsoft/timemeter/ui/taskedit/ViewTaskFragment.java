package com.simbirsoft.timemeter.ui.taskedit;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.be.android.library.worker.util.JobSelector;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.EventListener;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.controller.ITaskActivityManager;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.events.TaskActivityStoppedEvent;
import com.simbirsoft.timemeter.events.TaskActivityUpdateEvent;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTaskBundleJob;
import com.simbirsoft.timemeter.jobs.LoadTaskRecentActivitiesJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.activities.EditTaskActivityDialogFragment;
import com.simbirsoft.timemeter.ui.activities.TaskActivitiesAdapter;
import com.simbirsoft.timemeter.ui.activities.TaskActivitiesFragment;
import com.simbirsoft.timemeter.ui.activities.TaskActivitiesFragment_;
import com.simbirsoft.timemeter.ui.activities.TaskActivitiesLayoutManager;
import com.simbirsoft.timemeter.ui.activities.TaskTimeSpanActions;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.base.DialogContainerActivity;
import com.simbirsoft.timemeter.ui.base.FragmentContainerActivity;
import com.simbirsoft.timemeter.ui.main.MainPageFragment;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.model.TaskRecentActivity;
import com.simbirsoft.timemeter.ui.util.TaskActivitiesSumTime;
import com.simbirsoft.timemeter.ui.util.TimeUtils;
import com.simbirsoft.timemeter.ui.views.ProgressLayout;
import com.simbirsoft.timemeter.ui.views.TagFlowView;
import com.simbirsoft.timemeter.ui.views.TagView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

@EFragment(R.layout.fragment_view_task)
public class ViewTaskFragment extends BaseFragment
        implements JobLoader.JobLoaderCallbacks {
    public static final String EXTRA_TASK_BUNDLE = "extra_task_bundle";
    public static final String EXTRA_TASK_ID = "extra_task_id";

    private static final String LOADER_TAG = "ViewTaskFragment_load_content";
    private static final String LOAD_TASK_BUNDLE_TAG = "ViewTaskFragment_load_task_bundle";

    private static final Logger LOG = LogFactory.getLogger(ViewTaskFragment.class);

    private static final int REQUEST_CODE_VIEW_ACTIVITIES = 101;
    private static final int REQUEST_CODE_EDIT_ACTIVITY = 10005;

    private static final String STATE_SELECTION = "asdasd";

    @ViewById(R.id.tagFlowView)
    protected TagFlowView tagFlowView;

    @ViewById(R.id.sumActivitiesTime)
    protected TextView sumActivitiesTime;

    @FragmentArg(EXTRA_TASK_BUNDLE)
    TaskBundle mExtraTaskBundle;

    @FragmentArg(EXTRA_TASK_ID)
    long mExtraTaskId = -1;

    private ITaskActivityManager taskActivityManager;

    private Menu menu;

    @InstanceState
    public long taskActivitiesSumTime = 0;

    private final TagView.TagViewClickListener mTagViewClickListener = (tagView) -> {
        LOG.debug("Tag <" + tagView.getTag().getName() + "> clicked!");
    };

    @ViewById(android.R.id.list)
    RecyclerView mRecyclerView;

    @ViewById(R.id.progressLayout)
    ProgressLayout mProgressLayout;

    @InstanceState
    int mListPosition = -1;

    @InstanceState
    int mListPositionOffset;

    List<Long> mSelectedSpans = new ArrayList<>();

    @Inject
    Bus mBus;

    private Bundle savedInstanceState;

    private TaskActivitiesAdapter mAdapter;
    private TaskTimeSpanActions mTaskTimeSpanActions;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mTaskTimeSpanActions.saveState(outState);

        long[] selection = Longs.toArray(mAdapter.getSelectedSpanIds());
        outState.putLongArray(STATE_SELECTION, selection);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setHasOptionsMenu(true);
        Injection.sUiComponent.injectViewTaskFragment(this);

        taskActivityManager = Injection.sTaskManager.taskActivityManager();

        if (savedInstanceState != null) {
            mSelectedSpans = Longs.asList(savedInstanceState.getLongArray(STATE_SELECTION));
        }

        calculateTaskActivitiesSumTime();

        mTaskTimeSpanActions = new TaskTimeSpanActions(getActivity(), savedInstanceState);
        mBus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTaskTimeSpanActions.dispose();
    }

    @Override
    public void onDestroyView() {
        mBus.unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_view_task, menu);

        this.menu = menu;

        updateUIAfterChangeTaskStatus();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mAdapter.getItemCount() > 0) {
            TaskActivitiesLayoutManager layoutManager = (TaskActivitiesLayoutManager) mRecyclerView.getLayoutManager();
            mListPosition = layoutManager.findFirstVisibleItemPosition();
            mListPositionOffset = (mListPosition != RecyclerView.NO_POSITION)
                                    ? layoutManager.findItemOffset(mListPosition) : 0;
        }

        taskActivityManager.saveTaskActivity();
    }

    @Override
    public void onStop() {
        super.onStop();
        mSelectedSpans = Lists.newArrayList(mAdapter.getSelectedSpanIds());
    }

    private void setActionBarTitleAndHome(String title) {
        ActionBar mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (title != null) {
            mActionBar.setTitle(title);
        }
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @AfterViews
    void bindViews() {
        View hintView = tagFlowView.getHintView();
        if (hintView != null) hintView.setOnClickListener((v) -> {
            goToEditTaskTagsScene();
        });

        mRecyclerView.setHasFixedSize(false);
        final TaskActivitiesLayoutManager layoutManager = new TaskActivitiesLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new TaskActivitiesAdapter(getActivity());

        if (mExtraTaskBundle == null) {
            requestLoad(LOAD_TASK_BUNDLE_TAG, this);
        } else {
            configureFragment();
        }
    }

    private void configureFragment() {
        setActionBarTitleAndHome(mExtraTaskBundle.getTask().getDescription());
        tagFlowView.bindTagViews(mExtraTaskBundle.getTags());
        tagFlowView.setTagViewsClickListener(mTagViewClickListener);
        if (mExtraTaskId == -1) {
            mAdapter.setHighlightedSpans(mExtraTaskBundle.getTaskTimeSpans());
        }
        mRecyclerView.setAdapter(mAdapter);
        mProgressLayout.setShouldDisplayEmptyIndicatorMessage(true);
        mProgressLayout.setEmptyIndicatorStyle(Typeface.ITALIC);
        final Resources res = getResources();
        mProgressLayout.setEmptyIndicatorTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen.empty_indicator_text_size));
        mProgressLayout.setEmptyIndicatorTextColor(res.getColor(R.color.empty_indicator));
        mProgressLayout.setProgressLayoutCallbacks(
                new ProgressLayout.JobProgressLayoutCallbacks(JobSelector.forJobTags(LOADER_TAG)) {
                    @Override
                    public boolean hasContent() {
                        return mAdapter.getItemCount() > 0;
                    }

                });
        requestLoad(LOADER_TAG, this);
        mProgressLayout.updateProgressView();

        mTaskTimeSpanActions.bind(mAdapter, mRecyclerView);
        mTaskTimeSpanActions.setOnEditListener(sender -> editSelectedSpan());
        mTaskTimeSpanActions.setOnDidRemoveListener(sender -> requestLoad(LOADER_TAG, this));
        mTaskTimeSpanActions.setOnDidRestoreSpansListener(sender -> requestLoad(LOADER_TAG, this));
    }

    private void goToEditTask() {
        Bundle args = new Bundle();
        args.putString(EditTaskFragment.EXTRA_TITLE, getString(R.string.title_edit_task));
        args.putLong(EditTaskFragment.EXTRA_TASK_ID, mExtraTaskBundle.getTask().getId());
        args.putBoolean(EditTaskFragment.EXTRA_GO_TO_EDIT_TAGS_SCENE, false);

        Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                getActivity(), EditTaskFragment_.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, MainPageFragment.REQUEST_CODE_PROCESS_TASK);
    }

    private void goToEditTaskTagsScene() {
        Bundle args = new Bundle();
        args.putString(EditTaskFragment.EXTRA_TITLE, getString(R.string.title_edit_task));
        args.putLong(EditTaskFragment.EXTRA_TASK_ID, mExtraTaskBundle.getTask().getId());
        args.putBoolean(EditTaskFragment.EXTRA_GO_TO_EDIT_TAGS_SCENE, true);

        Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                getActivity(), EditTaskFragment_.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, MainPageFragment.REQUEST_CODE_PROCESS_TASK);
    }

    private void goToActivities() {
        Bundle args = new Bundle();
        String title = mExtraTaskBundle.getTask().getDescription();
        if(title != null) {
            args.putString(TaskActivitiesFragment.EXTRA_TITLE, title);
        }
        args.putLong(TaskActivitiesFragment.EXTRA_TASK_ID, mExtraTaskBundle.getTask().getId());

        Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                getActivity(), TaskActivitiesFragment_.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, REQUEST_CODE_VIEW_ACTIVITIES);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                LOG.debug("task edit clicked");
                goToEditTask();
                return true;

            case R.id.activities:
                LOG.debug("task view activities clicked");
                goToActivities();
                return true;

            case android.R.id.home:
                LOG.debug("task view home clicked");
                getActivity().finish();
                return true;

            case R.id.status:
                LOG.debug("task view status clicked");
                doTaskActive();
                updateUIAfterChangeTaskStatus();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        getActivity().setResult(resultCode, data);
        switch (requestCode) {
            case MainPageFragment.REQUEST_CODE_PROCESS_TASK:
                if (resultCode == EditTaskFragment.RESULT_CODE_CANCELLED) {
                    LOG.debug("result: task edit cancelled");
                    return;
                }
                final TaskBundle bundle = data.getParcelableExtra(
                        EditTaskFragment.EXTRA_TASK_BUNDLE);

                switch (resultCode) {
                    case EditTaskFragment.RESULT_CODE_TASK_REMOVED:
                        LOG.debug("result: task removed");
                        getActivity().finish();
                        break;

                    case EditTaskFragment.RESULT_CODE_TASK_UPDATED:
                        LOG.debug("result: task updated");
                        tagFlowView.bindTagViews(bundle.getTags());
                        setActionBarTitleAndHome(bundle.getTask().getDescription());
                        break;
                }
                return;

            case REQUEST_CODE_VIEW_ACTIVITIES:
            case REQUEST_CODE_EDIT_ACTIVITY:
                requestLoad(LOADER_TAG, this);
                break;

            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnJobSuccess(LoadTaskRecentActivitiesJob.class)
    public void onLoadSuccess(LoadJobResult<TaskRecentActivity> result) {
        TaskRecentActivity recentActivity = result.getData();
        mAdapter.setItems(recentActivity.getList(), mSelectedSpans);
        if (mListPosition >=0) {
            TaskActivitiesLayoutManager layoutManager = (TaskActivitiesLayoutManager) mRecyclerView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(mListPosition, mListPositionOffset);
            mListPosition = -1;
            mListPositionOffset = 0;
        } else {
            scrollToSelectedSpan();
        }
        mProgressLayout.updateProgressView();
        if (mAdapter.getItemCount() == 0) {
            mProgressLayout.setEmptyIndicatorMessage(recentActivity.getEmptyIndicatorMessage(getResources()));
        } else {
            AlphaAnimation animation = new AlphaAnimation(0, 1);
            animation.setDuration(140);
            mRecyclerView.setAnimation(animation);
        }
    }

    @OnJobFailure(LoadTaskRecentActivitiesJob.class)
    public void onLoadFailed() {
        showToast(R.string.error_unable_to_load_task_activities);
    }

    @OnJobSuccess(LoadTaskBundleJob.class)
    public void onTaskBundleLoaded(LoadJobResult<TaskBundle> taskBundle) {
        mExtraTaskBundle = taskBundle.getData();

        configureFragment();
        calculateTaskActivitiesSumTime();
    }

    @OnJobFailure(LoadTaskBundleJob.class)
    public void onTaskBundleLoadFailure() {
        SnackbarManager.show(Snackbar.with(getActivity())
                .text(R.string.error_loading_data)
                .colorResource(R.color.lightRed)
                .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
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
                        if (isAdded()) {
                            getActivity().finish();
                        }
                    }
                }));
    }

    @Override
    public Job onCreateJob(String s) {
        switch (s) {
            case LOADER_TAG:
                LoadTaskRecentActivitiesJob loadTaskRecentActivitiesJob = Injection.sJobsComponent.loadTaskRecentActivitiesJob();
                loadTaskRecentActivitiesJob.setGroupId(JobManager.JOB_GROUP_UNIQUE);
                loadTaskRecentActivitiesJob.setTaskId(mExtraTaskBundle.getTask().getId());
                loadTaskRecentActivitiesJob.setTaskTimeSpans(mExtraTaskBundle.getTaskTimeSpans());
                loadTaskRecentActivitiesJob.addTag(LOADER_TAG);
                return loadTaskRecentActivitiesJob;
            case LOAD_TASK_BUNDLE_TAG:
                LoadTaskBundleJob loadTaskBundleJob = Injection.sJobsComponent.loadTaskBundleJob();
                loadTaskBundleJob.setTaskId(mExtraTaskId);
                loadTaskBundleJob.setGroupId(JobManager.JOB_GROUP_UNIQUE);
                return loadTaskBundleJob;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Click(R.id.activitiesTitleContainer)
    void activitiesTitleClicked() {
        goToActivities();
    }

    @Subscribe
    public void onTaskActivityUpdated(TaskActivityUpdateEvent event) {
        if (mExtraTaskBundle == null) {
            return;
        }

        final long taskId = event.getActiveTaskInfo().getTask().getId();
        if (mExtraTaskBundle.getTask().getId() != taskId) {
            return;
        }

        mAdapter.updateCurrentActivityTime(taskId);
        updateTaskActivitiesSumTime();
    }

    private void scrollToSelectedSpan() {
        int[] position = new int[2];
        if (mAdapter.getEarliestHighlightedSpanPosition(position)) {
            TaskActivitiesLayoutManager layoutManager = (TaskActivitiesLayoutManager) mRecyclerView.getLayoutManager();
            layoutManager.scrollToSpan(position[0], position[1]);
        }
    }

    private void editSelectedSpan() {
        List<TaskTimeSpan> selected = mAdapter.getSelectedSpans();
        Preconditions.checkArgument(selected.size() == 1, "there should be 1 selected span");
        onTaskTimeSpanEditClicked(selected.get(0));
    }

    public void onTaskTimeSpanEditClicked(TaskTimeSpan span) {
        Bundle args = new Bundle();
        args.putString(EditTaskActivityDialogFragment.EXTRA_TITLE, mExtraTaskBundle.getTask().getDescription());
        args.putLong(EditTaskActivityDialogFragment.EXTRA_SPAN_ID, span.getId());
        Intent launchIntent = DialogContainerActivity.prepareDialogLaunchIntent(
                getActivity(), EditTaskActivityDialogFragment.class.getName(), args);
        startActivityForResult(launchIntent, REQUEST_CODE_EDIT_ACTIVITY);
    }

    @Subscribe
    public void onTaskActivityStopped(TaskActivityStoppedEvent event) {
        taskActivityManager.stopTask(event.task);
        updateUIAfterChangeTaskStatus();
    }

    private void doTaskActive() {
        final Task task = mExtraTaskBundle.getTask();

        if (taskActivityManager.isTaskActive(task)) {
            taskActivityManager.stopTask(task);
        }
        else {
            taskActivityManager.startTask(task);
        }
    }

    private void updateUIAfterChangeTaskStatus() {
        if (menu == null) {
            return;
        }

        final MenuItem item = menu.findItem(R.id.status);

        if (item == null || mExtraTaskBundle == null || mExtraTaskBundle.getTask() == null) {
            return;
        }

        final Task task = mExtraTaskBundle.getTask();

        if (taskActivityManager.isTaskActive(task)) {
            setStopToolbarUI(item);
            setTaskActivitiesSumTime(TaskActivitiesSumTime.getSumHoursMinuteSecond(taskActivitiesSumTime));
        }
        else {
            setStartToolbarUI(item);
            setTaskActivitiesSumTime(TaskActivitiesSumTime.getSumHoursMinute(taskActivitiesSumTime));
        }
    }

    private void setStartToolbarUI(MenuItem item) {
        item.setIcon(R.drawable.ic_start_task);
        item.setTitle(R.string.action_start_task);
    }

    private void setStopToolbarUI(MenuItem item) {
        item.setIcon(R.drawable.ic_stop_task);
        item.setTitle(R.string.action_stop_task);
    }

    private void calculateTaskActivitiesSumTime() {
        if (savedInstanceState != null || mExtraTaskBundle == null || mExtraTaskBundle.getTaskTimeSpans() == null) {
            return;
        }

        final List<TaskTimeSpan> spans = mExtraTaskBundle.getTaskTimeSpans();
        taskActivitiesSumTime = TaskActivitiesSumTime.getSumTimeImMillis(spans);
    }

    private void setTaskActivitiesSumTime(String sum) {
        final String text = getResources().getString(R.string.task_activities_sum_time) + sum;
        sumActivitiesTime.setText(text);
    }

    private void updateTaskActivitiesSumTime() {
        taskActivitiesSumTime += TimeUtils.MILLIS_IN_SECOND;
        setTaskActivitiesSumTime(TaskActivitiesSumTime.getSumHoursMinuteSecond(taskActivitiesSumTime));
    }
}