package com.simbirsoft.timemeter.controller;

import com.simbirsoft.timemeter.db.model.Task;

public interface ITaskActivityManager extends ITaskActivityInfoProvider {
    public void startTask(Task task);
    public void stopTask(Task task);
    public void saveTaskActivity();
    public void addTaskActivityUpdateListener(TaskActivityTimerUpdateListener listener);
    public void removeTaskActivityUpdateListener(TaskActivityTimerUpdateListener listener);
}
