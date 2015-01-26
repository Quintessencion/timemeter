package com.simbirsoft.timemeter.injection.components;

import com.simbirsoft.timemeter.App;
import com.simbirsoft.timemeter.jobs.LoadTagListJob;
import com.simbirsoft.timemeter.jobs.LoadTaskBundleJob;
import com.simbirsoft.timemeter.jobs.LoadTaskListJob;
import com.simbirsoft.timemeter.jobs.LoadTaskTagsJob;
import com.simbirsoft.timemeter.jobs.RemoveTaskJob;
import com.simbirsoft.timemeter.jobs.SaveTaskBundleJob;

public interface JobsComponent {
    public App app();
    public LoadTaskListJob loadTaskListJob();
    public LoadTagListJob loadTagListJob();
    public LoadTaskTagsJob loadTaskTagsJob();
    public LoadTaskBundleJob loadTaskBundleJob();
    public SaveTaskBundleJob saveTaskBundleJob();
    public RemoveTaskJob removeTaskJob();
}
