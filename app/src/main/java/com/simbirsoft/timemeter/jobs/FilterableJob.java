package com.simbirsoft.timemeter.jobs;

import com.simbirsoft.timemeter.model.TaskLoadFilter;

public interface FilterableJob {
    public TaskLoadFilter getTaskLoadFilter();
    public void setTaskLoadFilter(TaskLoadFilter filter);
}
