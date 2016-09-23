package com.simbirsoft.timeactivity.controller;

import com.simbirsoft.timeactivity.db.model.Task;
import com.simbirsoft.timeactivity.db.model.TaskTimeSpan;

public class ActiveTaskInfo {
    private Task mTask;
    private TaskTimeSpan mCurrentTimeSpan;

    ActiveTaskInfo() {
    }

    ActiveTaskInfo(Task task, TaskTimeSpan timeSpan) {
        mTask = task;
        mCurrentTimeSpan = timeSpan;
    }

    ActiveTaskInfo(ActiveTaskInfo other) {
        this(other.mTask, other.mCurrentTimeSpan);
    }

    public Task getTask() {
        return mTask;
    }

    void setTask(Task task) {
        mTask = task;
    }

    public TaskTimeSpan getTaskTimeSpan() {
        return mCurrentTimeSpan;
    }

    public long getPastTimeMillis() {
        return System.currentTimeMillis() - mCurrentTimeSpan.getStartTimeMillis();
    }

    void setCurrentTimeSpan(TaskTimeSpan currentTimeSpan) {
        mCurrentTimeSpan = currentTimeSpan;
    }
}
