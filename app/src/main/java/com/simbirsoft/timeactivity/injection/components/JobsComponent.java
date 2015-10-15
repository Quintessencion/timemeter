package com.simbirsoft.timeactivity.injection.components;

import com.simbirsoft.timeactivity.App;
import com.simbirsoft.timeactivity.jobs.BackupTagJob;
import com.simbirsoft.timeactivity.jobs.LoadActivityCalendarJob;
import com.simbirsoft.timeactivity.jobs.LoadOverallTaskActivityTimeJob;
import com.simbirsoft.timeactivity.jobs.LoadPeriodActivitySplitTimeSumJob;
import com.simbirsoft.timeactivity.jobs.LoadPeriodActivityTimeSumJob;
import com.simbirsoft.timeactivity.jobs.LoadPeriodActivityTimelineJob;
import com.simbirsoft.timeactivity.jobs.LoadPeriodSplitActivityTimelineJob;
import com.simbirsoft.timeactivity.jobs.LoadStatisticsViewBinders;
import com.simbirsoft.timeactivity.jobs.LoadTagListJob;
import com.simbirsoft.timeactivity.jobs.LoadTaskActivitiesJob;
import com.simbirsoft.timeactivity.jobs.LoadTaskBundleJob;
import com.simbirsoft.timeactivity.jobs.LoadTaskListJob;
import com.simbirsoft.timeactivity.jobs.LoadTaskRecentActivitiesJob;
import com.simbirsoft.timeactivity.jobs.LoadTaskTagsJob;
import com.simbirsoft.timeactivity.jobs.LoadTaskTimespansJob;
import com.simbirsoft.timeactivity.jobs.LoadTasksForTimespansJob;
import com.simbirsoft.timeactivity.jobs.RemoveTagJob;
import com.simbirsoft.timeactivity.jobs.RemoveTaskJob;
import com.simbirsoft.timeactivity.jobs.SaveTagJob;
import com.simbirsoft.timeactivity.jobs.SaveTaskBundleJob;

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
    public LoadTaskRecentActivitiesJob loadTaskRecentActivitiesJob();
}
