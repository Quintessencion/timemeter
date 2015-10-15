package com.simbirsoft.timeactivity.events;

import com.simbirsoft.timeactivity.controller.ActiveTaskInfo;

public class TaskActivityUpdateEvent {

    private final ActiveTaskInfo mActiveTaskInfo;

    public TaskActivityUpdateEvent(ActiveTaskInfo activeTaskInfo) {
        mActiveTaskInfo = activeTaskInfo;
    }

    public ActiveTaskInfo getActiveTaskInfo() {
        return mActiveTaskInfo;
    }
}
