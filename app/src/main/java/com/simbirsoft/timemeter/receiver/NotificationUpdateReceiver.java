package com.simbirsoft.timemeter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.simbirsoft.timemeter.events.ScheduledTaskActivityNotificationUpdateEvent;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.log.LogFactory;
import com.squareup.otto.Bus;

import org.slf4j.Logger;

import javax.inject.Inject;

public class NotificationUpdateReceiver extends BroadcastReceiver {

    private static final Logger LOG = LogFactory.getLogger(NotificationUpdateReceiver.class);

    public static final String ACTION_REQUEST_NOTIFICATION_UPDATE =
            "com.simbirsoft.android.intent.action.REQUEST_NOTIFICATION_UPDATE";

    @Inject
    Bus mBus;

    public NotificationUpdateReceiver() {
        Injection.sTaskManager.injectNotificationUpdateReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_REQUEST_NOTIFICATION_UPDATE.equals(intent.getAction())) {
            LOG.warn("received unexpected broadcast intent action '{}'", intent.getAction());
            return;
        }

        LOG.trace("requested notification update via broadcast");
        mBus.post(new ScheduledTaskActivityNotificationUpdateEvent());
    }
}
