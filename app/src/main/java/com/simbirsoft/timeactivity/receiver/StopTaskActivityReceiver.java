package com.simbirsoft.timeactivity.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.simbirsoft.timeactivity.events.StopTaskActivityRequestedEvent;
import com.simbirsoft.timeactivity.injection.Injection;
import com.simbirsoft.timeactivity.log.LogFactory;
import com.squareup.otto.Bus;

import org.slf4j.Logger;

import javax.inject.Inject;

public class StopTaskActivityReceiver extends BroadcastReceiver {

    public static final String ACTION_STOP_TASK_ACTIVITY =
            "com.simbirsoft.android.intent.action.STOP_TASK_ACTIVITY";

    private static final Logger LOG = LogFactory.getLogger(StopTaskActivityReceiver.class);

    @Inject
    Bus mBus;

    public StopTaskActivityReceiver() {
        Injection.sUiComponent.injectStopTaskActivityReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (ACTION_STOP_TASK_ACTIVITY.equalsIgnoreCase(action)) {
            mBus.post(new StopTaskActivityRequestedEvent());
        } else {
            LOG.warn("received unexpected broadcast intent action: '{}'", action);
        }
    }
}
