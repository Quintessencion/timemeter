package com.simbirsoft.timeactivity.injection.components;

import com.simbirsoft.timeactivity.controller.ITaskActivityManager;
import com.squareup.otto.Bus;

public interface TaskManagerComponent {
    public Bus bus();
    public ITaskActivityManager taskActivityManager();
}
