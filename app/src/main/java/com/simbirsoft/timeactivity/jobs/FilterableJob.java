package com.simbirsoft.timeactivity.jobs;

import com.simbirsoft.timeactivity.model.TaskLoadFilter;

public interface FilterableJob {
    public TaskLoadFilter getTaskLoadFilter();
    public void setTaskLoadFilter(TaskLoadFilter filter);
}
