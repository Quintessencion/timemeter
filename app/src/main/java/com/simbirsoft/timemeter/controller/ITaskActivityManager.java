package com.simbirsoft.timemeter.controller;

import com.simbirsoft.timemeter.db.model.Task;

public interface ITaskActivityManager {
    public void startTask(Task task);
    public void stopTask(Task task);
    public boolean isTaskActive(Task task);
    public ActiveTaskInfo getActiveTaskInfo();
    public boolean hasActiveTask();
    public void saveTaskActivity();
    public void addTaskActivityUpdateListener(TaskActivityTimerUpdateListener listener);
    public void removeTaskActivityUpdateListener(TaskActivityTimerUpdateListener listener);
}
