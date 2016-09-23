package com.simbirsoft.timeactivity.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.simbirsoft.timeactivity.Consts;
import com.simbirsoft.timeactivity.events.ScheduledTaskActivityNotificationUpdateEvent;
import com.simbirsoft.timeactivity.events.ScheduledTaskUpdateTabContentEvent;
import com.simbirsoft.timeactivity.injection.Injection;
import com.simbirsoft.timeactivity.log.LogFactory;
import com.squareup.otto.Bus;

import org.slf4j.Logger;

import javax.inject.Inject;

public class NotificationUpdateReceiver extends BroadcastReceiver {

    private static final Logger LOG = LogFactory.getLogger(NotificationUpdateReceiver.class);

    public static final String ACTION_REQUEST_NOTIFICATION_UPDATE =
            "com.simbirsoft.android.intent.action.REQUEST_NOTIFICATION_UPDATE";

    private static long mUpdateTabContentTimestamp = 0;

    @Inject
    Bus mBus;

    public NotificationUpdateReceiver() {
        Injection.sUiComponent.injectNotificationUpdateReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_REQUEST_NOTIFICATION_UPDATE.equals(intent.getAction())) {
            LOG.warn("received unexpected broadcast intent action '{}'", intent.getAction());
            return;
        }

        LOG.trace("requested notification update via broadcast");
        mBus.post(new ScheduledTaskActivityNotificationUpdateEvent());

        long currentTime = SystemClock.elapsedRealtime();
        if (mUpdateTabContentTimestamp == 0) {
            mUpdateTabContentTimestamp = currentTime;
        }
        if ((currentTime - mUpdateTabContentTimestamp) >= Consts.UPDATE_TAB_CONTENT_INTERVAL) {
            mUpdateTabContentTimestamp = currentTime;
            mBus.post(new ScheduledTaskUpdateTabContentEvent());
        }
    }
}
