package com.simbirsoft.timemeter.injection.components;

import com.simbirsoft.timemeter.controller.ITaskActivityManager;
import com.simbirsoft.timemeter.receiver.BootUpReceiver;
import com.simbirsoft.timemeter.receiver.NotificationUpdateReceiver;
import com.simbirsoft.timemeter.receiver.ScreenLockReceiver;
import com.squareup.otto.Bus;

public interface TaskManagerComponent {
    public Bus bus();
    public ITaskActivityManager taskActivityManager();
    void inject(BootUpReceiver receiver);
}
