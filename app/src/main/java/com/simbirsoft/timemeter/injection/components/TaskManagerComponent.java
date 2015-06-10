package com.simbirsoft.timemeter.injection.components;

import com.simbirsoft.timemeter.controller.ITaskActivityManager;

import com.squareup.otto.Bus;

public interface TaskManagerComponent {
    public Bus bus();
    public ITaskActivityManager taskActivityManager();
}
