package com.simbirsoft.timeactivity.controller;

import com.simbirsoft.timeactivity.db.model.Task;

public interface ITaskActivityManager extends ITaskActivityInfoProvider {
    public void startTask(Task task);
    public void stopTask(Task task);
    public void saveTaskActivity();
}
