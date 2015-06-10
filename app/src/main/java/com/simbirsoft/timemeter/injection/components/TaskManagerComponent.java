package com.simbirsoft.timemeter.injection.components;

import com.simbirsoft.timemeter.controller.ITaskActivityManager;
import com.simbirsoft.timemeter.receiver.BootUpReceiver;
import com.squareup.otto.Bus;

public interface TaskManagerComponent {
    public Bus bus();
    public ITaskActivityManager taskActivityManager();
    void inject(BootUpReceiver receiver);
}
