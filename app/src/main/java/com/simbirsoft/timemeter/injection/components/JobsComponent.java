package com.simbirsoft.timemeter.injection.components;

import com.simbirsoft.timemeter.App;
import com.simbirsoft.timemeter.jobs.BackupTagJob;
import com.simbirsoft.timemeter.jobs.LoadActivityCalendarJob;
import com.simbirsoft.timemeter.jobs.LoadOverallTaskActivityTimeJob;
import com.simbirsoft.timemeter.jobs.LoadPeriodActivitySplitTimeSumJob;
import com.simbirsoft.timemeter.jobs.LoadPeriodActivityTimeSumJob;
import com.simbirsoft.timemeter.jobs.LoadPeriodActivityTimelineJob;
import com.simbirsoft.timemeter.jobs.LoadPeriodSplitActivityTimelineJob;
import com.simbirsoft.timemeter.jobs.LoadStatisticsViewBinders;
import com.simbirsoft.timemeter.jobs.LoadTagListJob;
import com.simbirsoft.timemeter.jobs.LoadTaskActivitiesJob;
import com.simbirsoft.timemeter.jobs.LoadTaskBundleJob;
import com.simbirsoft.timemeter.jobs.LoadTaskListJob;
import com.simbirsoft.timemeter.jobs.LoadTaskTagsJob;
import com.simbirsoft.timemeter.jobs.LoadTaskTimespansJob;
import com.simbirsoft.timemeter.jobs.LoadTasksForTimespansJob;
import com.simbirsoft.timemeter.jobs.RemoveTagJob;
import com.simbirsoft.timemeter.jobs.RemoveTaskJob;
import com.simbirsoft.timemeter.jobs.SaveTagJob;
import com.simbirsoft.timemeter.jobs.SaveTaskBundleJob;

public interface JobsComponent {
    public App app();
    public LoadTaskListJob loadTaskListJob();
    public LoadTagListJob loadTagListJob();
    public LoadTaskTagsJob loadTaskTagsJob();
    public LoadTaskBundleJob loadTaskBundleJob();
    public SaveTaskBundleJob saveTaskBundleJob();
    public RemoveTaskJob removeTaskJob();
    public SaveTagJob saveTagJob();
    public RemoveTagJob removeTagJob();
    public BackupTagJob backupTagJob();
    public LoadTaskTimespansJob loadTaskTimespansJob();
    public LoadOverallTaskActivityTimeJob loadOverallTaskActivityTimeJob();
    public LoadStatisticsViewBinders loadStatisticsViewBinders();
    public LoadPeriodActivityTimeSumJob loadPeriodActivityTimeJob();
    public LoadPeriodActivityTimelineJob loadPeriodActivityTimelineJob();
    public LoadPeriodActivitySplitTimeSumJob loadPeriodActivitySplitTimeSumJob();
    public LoadPeriodSplitActivityTimelineJob loadPeriodSplitActivityTimelineJob();
    public LoadActivityCalendarJob loadActivityCalendarJob();
    public LoadTaskActivitiesJob loadTaskActivitiesJob();
    public LoadTasksForTimespansJob loadTasksJob();
}
