package com.simbirsoft.timeactivity.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.simbirsoft.timeactivity.controller.ITaskActivityManager;
import com.simbirsoft.timeactivity.injection.Injection;
import com.simbirsoft.timeactivity.log.LogFactory;

import org.slf4j.Logger;

import javax.inject.Inject;

public class BootUpReceiver extends BroadcastReceiver {

    private static final Logger LOG = LogFactory.getLogger(BootUpReceiver.class);

    @Inject
    ITaskActivityManager mTaskActivityManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Injection.sTaskManager.inject(this);

        if (mTaskActivityManager.hasActiveTask()) {
            LOG.info("resumed task activity");
        }
    }
}
