package com.simbirsoft.timemeter.ui.tasklist;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
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
import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.EventListener;
import com.simbirsoft.timemeter.Consts;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.controller.ActiveTaskInfo;
import com.simbirsoft.timemeter.controller.ITaskActivityManager;
import com.simbirsoft.timemeter.controller.TaskActivityTimerUpdateListener;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.events.FilterViewStateChangeEvent;
import com.simbirsoft.timemeter.events.TaskActivityStoppedEvent;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTaskListJob;
import com.simbirsoft.timemeter.jobs.SaveTaskBundleJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.base.FragmentContainerActivity;
import com.simbirsoft.timemeter.ui.main.ContentFragmentCallbacks;
import com.simbirsoft.timemeter.ui.main.MainPagerAdapter;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.taskedit.EditTaskFragment;
import com.simbirsoft.timemeter.ui.taskedit.EditTaskFragment_;
import com.simbirsoft.timemeter.ui.util.TaskFilterPredicate;
import com.simbirsoft.timemeter.ui.views.FilterView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

@EFragment(R.layout.fragment_task_list)
public class TaskListFragment extends BaseFragment implements JobLoader.JobLoaderCallbacks,
        TaskListAdapter.TaskClickListener,
        TaskActivityTimerUpdateListener,
        MainPagerAdapter.PageTitleProvider {

    private static final Logger LOG = LogFactory.getLogger(TaskListFragment.class);

    private static final String SNACKBAR_TAG = "task_list_snackbar";
    private static final int REQUEST_CODE_EDIT_TASK = 100;

    private static final int COLUMN_COUNT_DEFAULT = 2;
    private int mColumnCount;

    @ViewById(android.R.id.list)
    RecyclerView mRecyclerView;

    @ViewById(R.id.taskListContentRoot)
    ViewGroup mTaskListContentRoot;

    @ViewById(android.R.id.empty)
    TextView mEmptyListIndicator;

    @Inject
    Bus mBus;

    @InstanceState
    int[] mTaskListPosition;

    @InstanceState
    FilterView.FilterState mFilterViewState;

    private FloatingActionButton mFloatingActionButton;
    private TaskListAdapter mTasksViewAdapter;
    private String mTaskListLoaderTag;
    private ITaskActivityManager mTaskActivityManager;
    private ContentFragmentCallbacks mCallbacks;

    private void onFloatingButtonClicked(View v) {
        LOG.info("floating button clicked");

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
            getActivity().startActivityForResult(launchIntent, REQUEST_CODE_EDIT_TASK);
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

        RecyclerView.LayoutManager tasksViewLayoutManager = new StaggeredGridLayoutManager(
                mColumnCount,
                StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(tasksViewLayoutManager);

        mTasksViewAdapter = new TaskListAdapter(mTaskActivityManager);
        mTasksViewAdapter.setTaskClickListener(this);
        mRecyclerView.setAdapter(mTasksViewAdapter);

        requestLoad(mTaskListLoaderTag, this);
    }

    @Override
    public void onDestroyView() {
        RelativeLayout containerRoot = mCallbacks.getContainerUnderlayView();
        containerRoot.removeView((View)mFloatingActionButton.getParent());

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

        mTaskListLoaderTag = getClass().getName() + "_loader_tag";
        mTaskActivityManager = Injection.sTaskManager.taskActivityManager();
        mBus.register(this);
    }

    @Override
    public void onDestroy() {
        mBus.unregister(this);

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
    public void onResume() {
        super.onResume();

        mTaskActivityManager.addTaskActivityUpdateListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mTaskListPosition = new int[mColumnCount];

        if (mTasksViewAdapter.getItemCount() > 0) {
            mTaskListPosition = ((StaggeredGridLayoutManager)
                    mRecyclerView.getLayoutManager()).findFirstVisibleItemPositions(mTaskListPosition);
        }

        mTaskActivityManager.removeTaskActivityUpdateListener(this);
        mTaskActivityManager.saveTaskActivity();
    }

    private void removeTaskFromList(long taskId) {
        if (mTasksViewAdapter == null) return;

        mTasksViewAdapter.removeItems(taskId);
    }

    private void addTaskToList(TaskBundle task) {
        if (mTasksViewAdapter == null) return;

        if (mFilterViewState != null) {
            TaskFilterPredicate predicate = new TaskFilterPredicate(mFilterViewState);
            if (!predicate.apply(task)) {
                LOG.debug("created task isn't match current filter");
                return;
            }
        }

        mTasksViewAdapter.addFirstItem(task);
    }

    private void replaceTaskInList(TaskBundle task) {
        if (mTasksViewAdapter == null) return;

        mTasksViewAdapter.replaceItem(task);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_EDIT_TASK:
                if (resultCode == EditTaskFragment.RESULT_CODE_CANCELLED) {
                    LOG.debug("result: task edit cancelled");
                    return;
                }
                final long taskId = data.getLongExtra(EditTaskFragment.EXTRA_TASK_ID, -1);
                final TaskBundle bundle = data.getParcelableExtra(
                        EditTaskFragment.EXTRA_TASK_BUNDLE);

                switch (resultCode) {
                    case EditTaskFragment.RESULT_CODE_TASK_CREATED:
                        LOG.debug("result: task created");
                        addTaskToList(bundle);
                        break;
                    case EditTaskFragment.RESULT_CODE_TASK_RECREATED:
                        LOG.debug("result: task recreated");
                        requestLoad(mTaskListLoaderTag, this);
                        break;

                    case EditTaskFragment.RESULT_CODE_TASK_REMOVED:
                        LOG.debug("result: task removed");
                        removeTaskFromList(taskId);
                        showTaskRemoveUndoBar(bundle);
                        break;

                    case EditTaskFragment.RESULT_CODE_TASK_UPDATED:
                        LOG.debug("result: task updated");
                        replaceTaskInList(bundle);
                        break;
                }
                return;

            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnJobSuccess(LoadTaskListJob.class)
    public void onTaskListLoaded(LoadJobResult<List<TaskBundle>> event) {
        mTasksViewAdapter.setItems(event.getData());

        if (mTaskListPosition != null) {
            mRecyclerView.getLayoutManager().scrollToPosition(mTaskListPosition[0]);
            mTaskListPosition = null;
        }

        if ((mFilterViewState == null || mFilterViewState.isEmpty())
                && mTasksViewAdapter.getItemCount() == 0) {

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
        job.setGroupId(JobManager.JOB_GROUP_UNIQUE);

        if (mFilterViewState != null) {
            job.getTaskLoadFilter()
                    .tags(mFilterViewState.tags)
                    .dateMillis(mFilterViewState.dateMillis)
                    .period(mFilterViewState.period)
                    .searchText(mFilterViewState.searchText);
        }

        return job;
    }

    @Override
    public void onTaskCardClicked(TaskBundle item) {
        LOG.info("task card clicked; task {}", item);

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
    public void onTaskEditClicked(TaskBundle item) {
        LOG.debug("edit task: {}", item);

        SnackbarManager.dismiss();

        Bundle args = new Bundle();
        args.putString(EditTaskFragment.EXTRA_TITLE, getString(R.string.title_edit_task));
        args.putLong(EditTaskFragment.EXTRA_TASK_ID, item.getTask().getId());

        Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                getActivity(), EditTaskFragment_.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, REQUEST_CODE_EDIT_TASK);
    }

    @Override
    public void onTaskEditLongClicked(TaskBundle item, View itemView) {
        showToastWithAnchor(R.string.hint_edit_task, itemView);
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

        mRecyclerView.postDelayed(() -> {
            try {
                SaveTaskBundleJob job = Injection.sJobsComponent.saveTaskBundleJob();
                job.setTaskBundle((TaskBundle) unmarshallTask.get());
                submitJob(job);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, delay);
    }

    @Subscribe
    public void onFilterViewStateChanged(FilterViewStateChangeEvent ev) {
        mFilterViewState = ev.getFilterState();

        requestLoad(mTaskListLoaderTag, this);
    }

    @OnJobSuccess(SaveTaskBundleJob.class)
    public void onTaskSaved() {
        requestLoad(mTaskListLoaderTag, this);
    }

    @OnJobFailure(SaveTaskBundleJob.class)
    public void onTaskSaveFailed() {
        Snackbar bar = Snackbar.with(getActivity())
                .text(R.string.error_unable_to_backup_task)
                .colorResource(R.color.lightRed)
                .attachToRecyclerView(mRecyclerView)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE);
        bar.setTag(SNACKBAR_TAG);
        SnackbarManager.show(bar);
    }

    private void showTaskRemoveUndoBar(TaskBundle bundle) {
        // Hide floating action button
        mFloatingActionButton.hide(false);

        final Snackbar bar = Snackbar.with(getActivity())
                .type(SnackbarType.MULTI_LINE)
                .actionLabel(R.string.action_undo_remove)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                .attachToRecyclerView(mRecyclerView)
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
                        // Reattach floating action button
                        mFloatingActionButton.attachToRecyclerView(mRecyclerView);
                        mFloatingActionButton.show(true);
                    }
                });

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
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

                return sb.toString();
            }

            @Override
            protected void onPostExecute(String text) {
                if (isAdded() && !getActivity().isFinishing()) {
                    bar.text(text);
                    SnackbarManager.show(bar);
                }
            }
        }.execute();
    }

    @Override
    public void onTaskActivityUpdate(ActiveTaskInfo info) {
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
}

