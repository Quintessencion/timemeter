package com.simbirsoft.timemeter.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.EventListener;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.controller.ITaskActivityManager;
import com.simbirsoft.timemeter.controller.ActiveTaskInfo;
import com.simbirsoft.timemeter.controller.TaskActivityTimerUpdateListener;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTaskListJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.base.FragmentContainerActivity;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.taskedit.EditTaskFragment;
import com.simbirsoft.timemeter.ui.taskedit.EditTaskFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;

@EFragment(R.layout.fragment_task_list)
public class TaskListFragment extends BaseFragment implements JobLoader.JobLoaderCallbacks,
        TaskListAdapter.TaskClickListener, TaskActivityTimerUpdateListener {

    private static final Logger LOG = LogFactory.getLogger(TaskListFragment.class);

    private static final int REQUEST_CODE_EDIT_TASK = 100;

    private static final int COLUMN_COUNT_DEFAULT = 2;
    private static final int DISMISS_DELAY_MILLIS = 500;
    private int mColumnCount;

    public static TaskListFragment newInstance() {
        return new TaskListFragment_();
    }

    @ViewById(android.R.id.list)
    RecyclerView mRecyclerView;

    @ViewById(R.id.floatingButton)
    FloatingActionButton mFloatingActionButton;

    @InstanceState
    int[] mTagListPosition;

    private TaskListAdapter mTasksViewAdapter;
    private String mTaskListLoaderTag;
    private ITaskActivityManager mTaskActivityManager;

    @Click(R.id.floatingButton)
    void onFloatingButtonClicked(View v) {
        LOG.info("floating button clicked");

        Snackbar current = SnackbarManager.getCurrentSnackbar();
        long delay = 0;
        if (current != null && current.isShowing()) {
            delay = DISMISS_DELAY_MILLIS;
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

    @LongClick(R.id.floatingButton)
    void onFloatingActionButtonLongClicked(View v) {
        showToast(R.string.hint_new_task);
    }


    @AfterViews
    void bindViews() {
        mFloatingActionButton.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setHasFixedSize(false);

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTaskListLoaderTag = getClass().getName() + "_loader_tag";
        mTaskActivityManager = Injection.sTaskManager.taskActivityManager();
    }

    @Override
    public void onDestroy() {
        if (mTasksViewAdapter != null) {
            mTasksViewAdapter.setTaskClickListener(null);
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

        mTagListPosition = new int[mColumnCount];

        if (mTasksViewAdapter.getItemCount() > 0) {
            mTagListPosition = ((StaggeredGridLayoutManager)
                    mRecyclerView.getLayoutManager()).findFirstVisibleItemPositions(mTagListPosition);
        }

        mTaskActivityManager.removeTaskActivityUpdateListener(this);
        mTaskActivityManager.saveTaskActivity();
    }

    private void removeTaskFromList(long taskId) {
        mTasksViewAdapter.removeItems(taskId);
    }

    private void addTaskToList(TaskBundle task) {
        mTasksViewAdapter.addFirstItem(task);
    }

    private void replaceTaskInList(TaskBundle task) {
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

        if (mTagListPosition != null) {
            mRecyclerView.getLayoutManager().scrollToPosition(mTagListPosition[0]);
            mTagListPosition = null;
        }
    }

    @OnJobFailure(LoadTaskListJob.class)
    public void onTaskListLoadFailed() {
        showToast(R.string.error_unable_to_load_task_list);
    }

    @Override
    public Job onCreateJob(String loaderAttachTag) {
        return Injection.sJobsComponent.loadTaskListJob();
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
            delay = DISMISS_DELAY_MILLIS;
            snackbar.dismiss();
        }

        mRecyclerView.postDelayed(() -> {
            Bundle args = new Bundle();
            args.putString(EditTaskFragment.EXTRA_TITLE, getString(R.string.title_edit_task));
            args.putParcelable(EditTaskFragment.EXTRA_TASK_BUNDLE, taskBundle);

            Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                    getActivity(), EditTaskFragment_.class.getName(), args);
            getActivity().startActivityForResult(launchIntent, REQUEST_CODE_EDIT_TASK);
        }, delay);
    }

    private void showTaskRemoveUndoBar(TaskBundle bundle) {
        // Hide floating action button
        mFloatingActionButton.hide(false);

        final String description = bundle.getTask().getDescription();
        final String undoMessage = getString(R.string.hint_task_removed)
                + "\n"
                + description;

        final SpannableStringBuilder sb = new SpannableStringBuilder(undoMessage);
        final StyleSpan iss = new StyleSpan(android.graphics.Typeface.ITALIC);
        sb.setSpan(iss, undoMessage.length() - description.length(),
                undoMessage.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        Snackbar bar = Snackbar.with(getActivity())
                .type(SnackbarType.MULTI_LINE)
                .text(sb)
                .actionLabel(R.string.action_undo_remove)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                .attachToRecyclerView(mRecyclerView)
                .color(getResources().getColor(R.color.primaryDark))
                .actionListener((snackbar) -> backupRemovedTask(bundle, snackbar))
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
        SnackbarManager.show(bar);
    }

    @Override
    public void onTaskActivityUpdate(ActiveTaskInfo info) {
        if (mRecyclerView != null && mTasksViewAdapter != null) {
            mTasksViewAdapter.updateItemView(mRecyclerView, info.getTask());
        }
    }
}
