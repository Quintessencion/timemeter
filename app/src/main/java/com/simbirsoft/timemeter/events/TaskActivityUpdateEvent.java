package com.simbirsoft.timemeter.events;

import com.simbirsoft.timemeter.controller.ActiveTaskInfo;

public class TaskActivityUpdateEvent {

    private final ActiveTaskInfo mActiveTaskInfo;

    public TaskActivityUpdateEvent(ActiveTaskInfo activeTaskInfo) {
        mActiveTaskInfo = activeTaskInfo;
    }

    public ActiveTaskInfo getActiveTaskInfo() {
        return mActiveTaskInfo;
    }
}
