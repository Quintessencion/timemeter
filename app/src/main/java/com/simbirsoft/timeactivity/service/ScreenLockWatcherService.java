package com.simbirsoft.timeactivity.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.simbirsoft.timeactivity.log.LogFactory;
import com.simbirsoft.timeactivity.receiver.ScreenLockReceiver;

import org.slf4j.Logger;

public class ScreenLockWatcherService extends Service {

    private static final Logger LOG = LogFactory.getLogger(ScreenLockWatcherService.class);

    private static ScreenLockWatcherService sInstance;

    private ScreenLockReceiver mScreenLockReceiver;
    private boolean mIsStarted;

    public static ScreenLockWatcherService getInstance() {
        return sInstance;
    }

    public static boolean isStarted() {
        if (sInstance == null) {
            return false;
        }

        return sInstance.mIsStarted;
    }

    public static void start(Context context) {
        context.startService(new Intent(context, ScreenLockWatcherService.class));
    }

    public static void stop(Context context) {
        if (sInstance != null && sInstance.mIsStarted) {
            try {
                context.stopService(new Intent(context, ScreenLockWatcherService.class));
            } catch (Exception e) {
                LOG.error("unable to stop screen lock watcher service");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mIsStarted = true;
        sInstance = this;
        mScreenLockReceiver = new ScreenLockReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenLockReceiver, filter);
        LOG.info("started screen lock watcher service");
    }

    @Override
    public void onDestroy() {
        mIsStarted = false;
        unregisterReceiver(mScreenLockReceiver);
        super.onDestroy();
        LOG.info("stopped screen lock watcher service");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
