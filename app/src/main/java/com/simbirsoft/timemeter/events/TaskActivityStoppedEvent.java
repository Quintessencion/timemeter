package com.simbirsoft.timemeter.events;

import com.simbirsoft.timemeter.db.model.Task;

public class TaskActivityStoppedEvent {
    public Task task;

    public TaskActivityStoppedEvent(Task task) {
        this.task = task;
    }

}
