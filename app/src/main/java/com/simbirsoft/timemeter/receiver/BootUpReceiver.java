package com.simbirsoft.timemeter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.simbirsoft.timemeter.injection.Injection;

public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent ativivtyIntent = new Intent(context, Injection.sTaskManager.taskActivityManager().getClass());
        ativivtyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(ativivtyIntent);
    }
}
