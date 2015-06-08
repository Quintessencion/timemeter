package com.simbirsoft.timemeter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.simbirsoft.timemeter.ui.main.MainActivity_;

public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent ativivtyIntent = new Intent(context, MainActivity_.class);
        ativivtyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(ativivtyIntent);

//        Intent serviceIntent = new Intent(context, MyService.class);
//        context.startService(serviceIntent);
    }
}
