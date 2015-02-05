package com.simbirsoft.timemeter.controller;

import com.simbirsoft.timemeter.db.model.Task;

public interface ITaskActivityInfoProvider {
    public boolean isTaskActive(Task task);
    public ActiveTaskInfo getActiveTaskInfo();
    public boolean hasActiveTask();
}
