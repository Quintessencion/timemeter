package com.simbirsoft.timemeter.events;

import com.simbirsoft.timemeter.controller.ActiveTaskInfo;

public class TaskActivityStartedEvent {

    public ActiveTaskInfo mActiveTaskInfo;

    public TaskActivityStartedEvent(ActiveTaskInfo info) {
        mActiveTaskInfo = info;
    }
}
