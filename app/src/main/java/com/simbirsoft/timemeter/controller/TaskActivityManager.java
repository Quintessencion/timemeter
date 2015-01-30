package com.simbirsoft.timemeter.controller;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.annotations.OnJobEvent;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.handlers.JobEventDispatcher;
import com.be.android.library.worker.interfaces.Job;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.Consts;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.jobs.UpdateTaskActivityTimerJob;
import com.simbirsoft.timemeter.log.LogFactory;

import org.slf4j.Logger;

import java.util.LinkedList;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TaskActivityManager implements ITaskActivityManager {

    private static final Logger LOG = LogFactory.getLogger(TaskActivityManager.class);

    private final DatabaseHelper mDatabaseHelper;

    private final LinkedList<TaskActivityTimerUpdateListener> mActivityUpdateListeners;
    private ActiveTaskInfo mActiveTaskInfo;
    private boolean mIsInitialized;
    private final String mUpdateJobTag = TaskActivityManager.class.getName() + "_TAG_UPDATE_JOB";
    private final JobEventDispatcher mJobEventDispatcher;
    private long mLastSaveActivityTimeMillis;

    @Inject
    public TaskActivityManager(Context context, DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
        LOG.info("{} created", getClass().getName());
        mActivityUpdateListeners = Lists.newLinkedList();
        mJobEventDispatcher = new JobEventDispatcher(context, "TaskActivityManager_Dispatcher");
    }

    public static ActiveTaskInfo fetchLastTaskActivity(Task task, SQLiteDatabase db) {
        TaskTimeSpan span = cupboard().withDatabase(db)
                .query(TaskTimeSpan.class)
                .withSelection("taskId=?", String.valueOf(task.getId()))
                .groupBy("startTimeMillis")
                .having("max(startTimeMillis)")
                .get();

        if (span == null) {
            return null;
        }

        return new ActiveTaskInfo(task, span);
    }

    public void initialize() {
        Preconditions.checkState(!mIsInitialized, "already initialized");
        LOG.debug("initialization started");

        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        mActiveTaskInfo = fetchRunningTask(db);

        if (mActiveTaskInfo != null) {
            requestTimerUpdates();
            updateTaskActivityRecord();
        }

        mJobEventDispatcher.register(this);

        mIsInitialized = true;

        LOG.debug("initialization finished");
    }

    private boolean isSaveActivityTimeoutReached() {
        return System.currentTimeMillis() - mLastSaveActivityTimeMillis
                > Consts.TASK_ACTIVITY_SAVE_PERIOD_MILLIS;
    }

    @Override
    public void startTask(Task task) {
        Preconditions.checkState(mIsInitialized, "task activity manager is not initialized");
        Preconditions.checkArgument(task != null, "task may not be null");

        if (hasActiveTask()) {
            if (!mActiveTaskInfo.getTask().equals(task)) {
                stopTaskActivity();
            } else {
                LOG.warn("startTask(): specified task is already started");
                return;
            }
        }

        beginTaskActivity(task);
    }

    @Override
    public void stopTask(Task task) {
        Preconditions.checkState(mIsInitialized, "task activity manager is not initialized");
        Preconditions.checkArgument(task != null, "task may not be null");

        if (!hasActiveTask()) {
            LOG.warn("stopTask(): no active task");
            return;
        }

        Preconditions.checkState(mActiveTaskInfo.getTask().equals(task), "specified task is not active");

        stopTaskActivity();
    }

    @Override
    public boolean isTaskActive(Task task) {
        return hasActiveTask() && Objects.equal(mActiveTaskInfo.getTask(), task);
    }

    @Override
    public ActiveTaskInfo getActiveTaskInfo() {
        if (!hasActiveTask()) {
            return null;
        }

        return mActiveTaskInfo;
    }

    @Override
    public boolean hasActiveTask() {
        return mActiveTaskInfo != null;
    }

    @Override
    public void saveTaskActivity() {
        updateTaskActivityRecord();
    }

    @OnJobEvent(UpdateTaskActivityTimerJob.class)
    public void onJobActivityTimerUpdateEvent(UpdateTaskActivityTimerJob.UpdateTimerEvent event) {
        ActiveTaskInfo info = getActiveTaskInfo();
        if (info == null || !hasActiveTask()) {
            return;
        }

        if (isSaveActivityTimeoutReached()) {
            updateTaskActivityRecord();
        }

        for (TaskActivityTimerUpdateListener listener : mActivityUpdateListeners) {
            listener.onTaskActivityUpdate(info);
        }
    }

    @Override
    public void addTaskActivityUpdateListener(TaskActivityTimerUpdateListener listener) {
        mActivityUpdateListeners.add(listener);
    }

    @Override
    public void removeTaskActivityUpdateListener(TaskActivityTimerUpdateListener listener) {
        mActivityUpdateListeners.remove(listener);
    }

    private void beginTaskActivity(Task task) {
        if (hasActiveTask()) {
            stopTaskActivity();
        }

        Preconditions.checkArgument(mActiveTaskInfo == null,
                "running task info should be cleared");

        TaskTimeSpan span = new TaskTimeSpan();
        span.setActive(true);
        span.setStartTimeMillis(System.currentTimeMillis());
        span.setTaskId(task.getId());
        mActiveTaskInfo = new ActiveTaskInfo(task, span);
        LOG.info("new task activity created; task {}", task);

        requestTimerUpdates();

        updateTaskActivityRecord();
        LOG.debug("task activity started");
    }

    private void requestTimerUpdates() {
        Job job = JobManager.getInstance().findJob(mUpdateJobTag);
        if (job != null) {
            job.cancel();
        }
        job = new UpdateTaskActivityTimerJob(this);
        mJobEventDispatcher.submitJob(job);
    }

    private void stopTaskActivity() {
        if (!hasActiveTask()) {
            LOG.warn("stopTaskActivity(): no active task");
            return;
        }

        Job job = JobManager.getInstance().findJob(mUpdateJobTag);
        if (job != null) {
            job.cancel();
        }

        mActiveTaskInfo.getTaskTimeSpan().setActive(false);
        updateTaskActivityRecord();
        mActiveTaskInfo = null;
        LOG.debug("task activity stopped");
    }

    private void updateTaskActivityRecord() {
        if (!hasActiveTask()) {
            LOG.warn("updateTaskActivityRecord(): no active task");
            return;
        }

        final long currentTime = System.currentTimeMillis();
        TaskTimeSpan currentSpan = mActiveTaskInfo.getTaskTimeSpan();
        currentSpan.setEndTimeMillis(currentTime);

        cupboard().withDatabase(mDatabaseHelper.getWritableDatabase())
                  .put(currentSpan);
        mLastSaveActivityTimeMillis = currentTime;
        LOG.debug("task activity updated");
    }

    private static ActiveTaskInfo fetchRunningTask(SQLiteDatabase db) {
        TaskTimeSpan span = cupboard().withDatabase(db)
                .query(TaskTimeSpan.class)
                .withSelection("isActive=?", "1")
                .get();

        if (span == null) {
            return null;
        }

        Task task = cupboard().withDatabase(db).get(Task.class, span.getTaskId());

        if (task == null) {
            return null;
        }

        return new ActiveTaskInfo(task, span);
    }
}
