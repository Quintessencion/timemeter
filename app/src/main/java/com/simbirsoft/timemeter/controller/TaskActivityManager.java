package com.simbirsoft.timemeter.controller;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.annotations.OnJobEvent;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.handlers.JobEventDispatcher;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.util.JobSelector;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.Consts;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.events.ScheduledTaskUpdateTabContentEvent;
import com.simbirsoft.timemeter.events.StopTaskActivityRequestedEvent;
import com.simbirsoft.timemeter.events.TaskActivityStartedEvent;
import com.simbirsoft.timemeter.events.TaskActivityStoppedEvent;
import com.simbirsoft.timemeter.jobs.UpdateTaskActivityTimerJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.phrase.Phrase;

import org.slf4j.Logger;

import java.util.LinkedList;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TaskActivityManager implements ITaskActivityManager {

    private static final Logger LOG = LogFactory.getLogger(TaskActivityManager.class);

    private final Context mContext;
    private final DatabaseHelper mDatabaseHelper;
    private final TaskNotificationManager mTaskNotificationManager;
    private final Bus mBus;

    private final LinkedList<TaskActivityTimerUpdateListener> mActivityUpdateListeners;
    private ActiveTaskInfo mActiveTaskInfo;
    private boolean mIsInitialized;
    private final String mUpdateJobTag = TaskActivityManager.class.getName() + "_TAG_UPDATE_JOB";
    private final JobEventDispatcher mJobEventDispatcher;
    private long mLastSaveActivityTimeMillis;

    @Inject
    public TaskActivityManager(Context context, Bus bus, DatabaseHelper databaseHelper) {
        mContext = context;
        mBus = bus;
        mDatabaseHelper = databaseHelper;
        mTaskNotificationManager = new TaskNotificationManager(mContext, mBus, this);
        LOG.info("{} created", getClass().getName());
        mActivityUpdateListeners = Lists.newLinkedList();
        mJobEventDispatcher = new JobEventDispatcher(mContext, "TaskActivityManager_Dispatcher");
        mBus.register(this);
    }

    public static ActiveTaskInfo fetchLastTaskActivity(Task task, SQLiteDatabase db) {
        TaskTimeSpan span = cupboard().withDatabase(db)
                .query(TaskTimeSpan.class)
                .withSelection(TaskTimeSpan.COLUMN_TASK_ID + "=?", String.valueOf(task.getId()))
                .groupBy(TaskTimeSpan.COLUMN_START_TIME)
                .having("MAX(" + TaskTimeSpan.COLUMN_START_TIME + ")")
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
            resumeTaskActivity();
        }

        mJobEventDispatcher.register(this);

        mIsInitialized = true;

        LOG.debug("initialization finished");
    }

    @Subscribe
    public void onTaskActivityStopRequestedEvent(StopTaskActivityRequestedEvent event) {
        stopTaskActivity();
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

    private void resumeTaskActivity() {
        if (mActiveTaskInfo == null) {
            LOG.error("unable to resume task activity: no active task");
            return;
        }
        requestTimerUpdates();
        updateTaskActivityRecord();
        mTaskNotificationManager.updateTaskNotification(mActiveTaskInfo);
        LOG.debug("task activity resumed: '{}'", mActiveTaskInfo.getTask().getDescription());
        mBus.post(new TaskActivityStartedEvent(mActiveTaskInfo));
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
        LOG.info("new task activity created: '{}'", task.getDescription());
        resumeTaskActivity();
        mBus.post(new ScheduledTaskUpdateTabContentEvent());
        LOG.debug("task activity started");
    }

    private void requestTimerUpdates() {
        JobManager.getInstance().cancelAll(JobSelector.forJobTags(mUpdateJobTag));

        mJobEventDispatcher.submitJob(new UpdateTaskActivityTimerJob(this));
    }

    private void stopTaskActivity() {
        if (!hasActiveTask()) {
            LOG.warn("stopTaskActivity(): no active task");
            return;
        }

        JobManager.getInstance().cancelAll(JobSelector.forJobTags(mUpdateJobTag));

        final TaskActivityStoppedEvent event = new TaskActivityStoppedEvent(mActiveTaskInfo.getTask());
        mActiveTaskInfo.getTaskTimeSpan().setActive(false);
        updateTaskActivityRecord();
        mActiveTaskInfo = null;
        mBus.post(event);
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
                .withSelection(Phrase.from("{is_active}=?")
                        .put("is_active", TaskTimeSpan.COLUMN_IS_ACTIVE)
                        .format()
                        .toString(), "1")
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
