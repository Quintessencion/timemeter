package com.simbirsoft.timeactivity.controller;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.annotations.OnJobEvent;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.handlers.JobEventDispatcher;
import com.be.android.library.worker.util.JobSelector;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.simbirsoft.timeactivity.Consts;
import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.db.model.Task;
import com.simbirsoft.timeactivity.db.model.TaskTimeSpan;
import com.simbirsoft.timeactivity.events.ScheduledTaskUpdateTabContentEvent;
import com.simbirsoft.timeactivity.events.ScreenLockStateChangedEvent;
import com.simbirsoft.timeactivity.events.StopTaskActivityRequestedEvent;
import com.simbirsoft.timeactivity.events.TaskActivityStartedEvent;
import com.simbirsoft.timeactivity.events.TaskActivityStoppedEvent;
import com.simbirsoft.timeactivity.events.TaskActivityUpdateEvent;
import com.simbirsoft.timeactivity.jobs.UpdateTaskActivityTimerJob;
import com.simbirsoft.timeactivity.log.LogFactory;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.phrase.Phrase;

import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

@Singleton
public class TaskActivityManager implements ITaskActivityManager {

    private static final Logger LOG = LogFactory.getLogger(TaskActivityManager.class);

    private final JobManager mJobManager;
    private final Context mContext;
    private final DatabaseHelper mDatabaseHelper;
    private final TaskNotificationManager mTaskNotificationManager;
    private final Bus mBus;

    private ActiveTaskInfo mActiveTaskInfo;
    private boolean mIsInitialized;
    private final String mUpdateJobTag = TaskActivityManager.class.getName() + "_TAG_UPDATE_JOB";
    private final JobEventDispatcher mJobEventDispatcher;
    private long mLastSaveActivityTimeMillis;

    @Inject
    public TaskActivityManager(JobManager jobManager, Context context, Bus bus, DatabaseHelper databaseHelper) {
        mJobManager = jobManager;
        mContext = context;
        mBus = bus;
        mDatabaseHelper = databaseHelper;
        mTaskNotificationManager = new TaskNotificationManager(mContext, mBus, this);
        LOG.info("{} created", getClass().getName());
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
            if (!mActiveTaskInfo.getTask().getId().equals(task.getId())) {
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

        if (!mActiveTaskInfo.getTask().getId().equals(task.getId())) {
            LOG.warn("specified task is not active");
            return;
        }

        stopTaskActivity();
    }

    @Override
    public boolean isTaskActive(Task task) {
        return hasActiveTask() && mActiveTaskInfo.getTask().getId().equals(task.getId()); //Objects.equal(mActiveTaskInfo.getTask(), task);
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

        mBus.post(new TaskActivityUpdateEvent(info));
    }

    @Subscribe
    public void onScreenLockStatusChangeEvent(ScreenLockStateChangedEvent event) {
        if (event.isScreenLocked) {
            mJobManager.cancelAll(JobSelector.forJobTags(mUpdateJobTag));
        } else if (hasActiveTask()) {
            requestTimerUpdates();
        }
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
        mJobManager.cancelAll(JobSelector.forJobTags(mUpdateJobTag));

        UpdateTaskActivityTimerJob job = new UpdateTaskActivityTimerJob(this);
        job.addTag(mUpdateJobTag);
        mJobEventDispatcher.submitJob(job);
    }

    private void stopTaskActivity() {
        if (!hasActiveTask()) {
            LOG.warn("stopTaskActivity(): no active task");
            return;
        }

        mJobManager.cancelAll(JobSelector.forJobTags(mUpdateJobTag));

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