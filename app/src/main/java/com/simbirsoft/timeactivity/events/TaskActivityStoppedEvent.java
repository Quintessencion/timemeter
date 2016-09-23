package com.simbirsoft.timeactivity.events;

import com.simbirsoft.timeactivity.db.model.Task;

public class TaskActivityStoppedEvent {
    public Task task;

    public TaskActivityStoppedEvent(Task task) {
        this.task = task;
    }

}
