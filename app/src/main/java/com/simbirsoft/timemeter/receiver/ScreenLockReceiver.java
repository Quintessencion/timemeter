package com.simbirsoft.timemeter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.simbirsoft.timemeter.events.ScreenLockStateChangedEvent;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.log.LogFactory;
import com.squareup.otto.Bus;

import org.slf4j.Logger;

import javax.inject.Inject;

public class ScreenLockReceiver extends BroadcastReceiver {

    private static final Logger LOG = LogFactory.getLogger(ScreenLockReceiver.class);

    @Inject
    Bus mBus;

    public ScreenLockReceiver() {
        Injection.sUiComponent.injectScreenLockReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        switch (action) {
            case Intent.ACTION_SCREEN_ON:
                handleScreenOn();
                break;

            case Intent.ACTION_SCREEN_OFF:
                handleScreenOff();
                break;

            default:
                LOG.warn("received unexpected broadcast intent action '{}'", action);
                break;
        }
    }

    private void handleScreenOn() {
        mBus.post(new ScreenLockStateChangedEvent(false));
    }

    private void handleScreenOff() {
        mBus.post(new ScreenLockStateChangedEvent(true));
    }
}
