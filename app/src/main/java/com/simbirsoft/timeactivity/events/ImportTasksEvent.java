package com.simbirsoft.timeactivity.events;


import com.simbirsoft.timeactivity.ui.model.TaskBundle;

import java.util.List;

public class ImportTasksEvent {

    public List<TaskBundle> tasks;

    public ImportTasksEvent(List<TaskBundle> tasks) {
        this.tasks = tasks;
    }
}
