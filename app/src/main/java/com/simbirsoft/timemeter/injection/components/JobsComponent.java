package com.simbirsoft.timemeter.injection.components;

import com.simbirsoft.timemeter.App;
import com.simbirsoft.timemeter.jobs.LoadTaskListJob;

public interface JobsComponent {
    public App app();
    public LoadTaskListJob loadTaskListJob();
}
