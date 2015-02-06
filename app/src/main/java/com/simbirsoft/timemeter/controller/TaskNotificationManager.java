package com.simbirsoft.timemeter.controller;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.events.ScheduledTaskActivityNotificationUpdateEvent;
import com.simbirsoft.timemeter.events.ScreenLockStateChangedEvent;
import com.simbirsoft.timemeter.events.TaskActivityStartedEvent;
import com.simbirsoft.timemeter.events.TaskActivityStoppedEvent;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.receiver.NotificationUpdateReceiver;
import com.simbirsoft.timemeter.receiver.StopTaskActivityReceiver;
import com.simbirsoft.timemeter.service.ScreenLockWatcherService;
import com.simbirsoft.timemeter.ui.main.MainActivity;
import com.simbirsoft.timemeter.ui.main.MainActivity_;
import com.simbirsoft.timemeter.ui.util.TimerTextFormatter;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

public class TaskNotificationManager {

    private static final Logger LOG = LogFactory.getLogger(TaskNotificationManager.class);
    private static final int NOTIFICATION_ID = 3905231;

    private boolean mIsForeground = true;
    private final Context mContext;
    private final Bus mBus;
    private final ITaskActivityInfoProvider mTaskActivityInfoProvider;
    private NotificationCompat.Builder mNotificationBuilder;

    public TaskNotificationManager(Context context, Bus bus,
                                   ITaskActivityInfoProvider taskActivityInfoProvider) {
        mContext = context;
        mBus = bus;
        mTaskActivityInfoProvider = taskActivityInfoProvider;

        if (mTaskActivityInfoProvider.hasActiveTask()) {
            if (!ScreenLockWatcherService.isStarted()) {
                ScreenLockWatcherService.start(mContext);
            }
        }

        mBus.register(this);
    }

    @Subscribe
    public void ontaskActivityNotificationUpdate(ScheduledTaskActivityNotificationUpdateEvent event) {
        updateTaskNotification(mTaskActivityInfoProvider.getActiveTaskInfo());
    }

    @Subscribe
    public void onScreenLockStatusChangeEvent(ScreenLockStateChangedEvent event) {
        if (event.isScreenLocked) {
            setForeground(false);
        } else {
            setForeground(true);
        }
    }

    @Subscribe
    public void onTaskActivityStarted(TaskActivityStartedEvent event) {
        if (!ScreenLockWatcherService.isStarted()) {
            ScreenLockWatcherService.start(mContext);
        }
        updateTaskNotification(event.mActiveTaskInfo);
    }

    @Subscribe
    public void onTaskActivityStopped(TaskActivityStoppedEvent event) {
        ScreenLockWatcherService.stop(mContext);
        updateTaskNotification(null);
    }

    public void updateTaskNotification(@Nullable ActiveTaskInfo taskInfo) {
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (taskInfo == null) {
            nm.cancel(NOTIFICATION_ID);
            mNotificationBuilder = null;
            LOG.debug("cancelled task notification");

            return;
        }

        String notificationText = taskInfo.getTask().getDescription();
        long pastTime = taskInfo.getPastTimeMillis();
        CharSequence timeText = TimerTextFormatter.formatTaskNotificatoinTimer(
                mContext.getResources(), pastTime);
        if (mNotificationBuilder == null) {
            mNotificationBuilder = createTaskActivityNotification(
                    timeText, notificationText);
        } else {
            mNotificationBuilder.setContentTitle(timeText);
        }

        nm.notify(NOTIFICATION_ID, mNotificationBuilder.build());

        if (mIsForeground) {
            scheduleNotificationUpdate();
        }
    }

    public void scheduleNotificationUpdate() {
        final int updateIntervalMillis = getCurrentUpdateIntervalInMillis();

        long time = SystemClock.elapsedRealtime() + updateIntervalMillis;
        PendingIntent intent = createNotificationUpdateIntent();
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME, time, intent);
        LOG.trace("scheduled next notification update through {}ms", updateIntervalMillis);
    }

    public boolean isForeground() {
        return mIsForeground;
    }

    public final void setForeground(boolean isForeground) {
        if (mIsForeground == isForeground) {
            return;
        }

        mIsForeground = isForeground;

        if (mIsForeground) {
            LOG.debug("moved to foreground state");
        } else {
            LOG.debug("moved to background state");
        }

        updateTaskNotification(mTaskActivityInfoProvider.getActiveTaskInfo());
    }

    private NotificationCompat.Builder createTaskActivityNotification(CharSequence notificationTitle, CharSequence notificationText) {
        Intent actionIntent = new Intent(mContext, MainActivity_.class);
        actionIntent.setAction(MainActivity.ACTION_SHOW_ACTIVE_TASK);
        actionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                mContext, 0, actionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent stopActionIntent = new Intent(mContext, StopTaskActivityReceiver.class);
        stopActionIntent.setAction(StopTaskActivityReceiver.ACTION_STOP_TASK_ACTIVITY);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                mContext, 0, stopActionIntent, 0);

        NotificationCompat.Action stopAction = new NotificationCompat.Action(
                R.drawable.ic_cancel_white_24dp,
                mContext.getString(R.string.action_stop_task),
                stopPendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        return builder.setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setSmallIcon(R.drawable.ic_timer_white_24dp)
                .setTicker(notificationText)
                .setContentIntent(pendingIntent)
                .addAction(stopAction);
    }

    private PendingIntent createNotificationUpdateIntent() {
        Intent updateIntent = new Intent(mContext, NotificationUpdateReceiver.class);
        updateIntent.setAction(NotificationUpdateReceiver.ACTION_REQUEST_NOTIFICATION_UPDATE);

        return PendingIntent.getBroadcast(mContext, 0,
                updateIntent, PendingIntent.FLAG_ONE_SHOT);
    }

    private int getCurrentUpdateIntervalInMillis() {
        if (mIsForeground) {
            return (int) TimeUnit.SECONDS.toMillis(1);
        } else {
            return (int) TimeUnit.MINUTES.toMillis(1);
        }
    }
}
