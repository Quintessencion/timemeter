package com.simbirsoft.timemeter.events;

import com.simbirsoft.timemeter.ui.model.TaskBundle;

import java.util.List;

public class ImportTasksEvent {

    public List<TaskBundle> tasks;

    public ImportTasksEvent(List<TaskBundle> tasks) {
        this.tasks = tasks;
    }
}
