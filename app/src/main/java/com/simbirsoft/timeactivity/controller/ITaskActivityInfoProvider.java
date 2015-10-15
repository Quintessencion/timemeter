package com.simbirsoft.timeactivity.controller;

import com.simbirsoft.timeactivity.db.model.Task;

public interface ITaskActivityInfoProvider {
    public boolean isTaskActive(Task task);
    public ActiveTaskInfo getActiveTaskInfo();
    public boolean hasActiveTask();
}
