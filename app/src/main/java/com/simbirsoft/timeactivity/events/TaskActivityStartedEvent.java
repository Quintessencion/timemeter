package com.simbirsoft.timeactivity.events;

import com.simbirsoft.timeactivity.controller.ActiveTaskInfo;

public class TaskActivityStartedEvent {

    public ActiveTaskInfo mActiveTaskInfo;

    public TaskActivityStartedEvent(ActiveTaskInfo info) {
        mActiveTaskInfo = info;
    }
}
