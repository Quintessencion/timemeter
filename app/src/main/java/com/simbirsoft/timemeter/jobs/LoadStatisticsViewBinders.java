package com.simbirsoft.timemeter.jobs;

import android.content.Context;

import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.model.TaskLoadFilter;
import com.simbirsoft.timemeter.model.TaskOverallActivity;
import com.simbirsoft.timemeter.ui.model.DailyActivityDuration;
import com.simbirsoft.timemeter.ui.model.DailyTaskActivityDuration;
import com.simbirsoft.timemeter.ui.stats.StatisticsViewBinder;
import com.simbirsoft.timemeter.ui.stats.binders.ActivityStackedTimelineBinder;
import com.simbirsoft.timemeter.ui.stats.binders.ActivityTimelineBinder;
import com.simbirsoft.timemeter.ui.stats.binders.OverallActivityTimePieBinder;

import java.util.List;

import javax.inject.Inject;

public class LoadStatisticsViewBinders extends LoadJob implements FilterableJob {

    private final DatabaseHelper mDatabaseHelper;
    private final Context mContext;
    private TaskLoadFilter mTaskLoadFilter;

    @Inject
    public LoadStatisticsViewBinders(DatabaseHelper databaseHelper, Context context) {
        mDatabaseHelper = databaseHelper;
        mContext = context;
        mTaskLoadFilter = new TaskLoadFilter();
    }

    @Override
    public TaskLoadFilter getTaskLoadFilter() {
        return mTaskLoadFilter;
    }

    @Override
    public void setTaskLoadFilter(TaskLoadFilter taskLoadFilter) {
        mTaskLoadFilter = taskLoadFilter;
    }

    @Override
    protected LoadJobResult<List<StatisticsViewBinder>> performLoad() throws Exception {
        final List<StatisticsViewBinder> results = Lists.newArrayList();

        // Load overall activity
        final LoadOverallTaskActivityTimeJob loadOverallTaskActivityTimeJob =
                new LoadOverallTaskActivityTimeJob(mContext, mDatabaseHelper);
        loadOverallTaskActivityTimeJob.setTaskLoadFilter(mTaskLoadFilter);

        ForkJoiner overallActivityJoiner = buildFork(loadOverallTaskActivityTimeJob)
                .groupOnTheSameGroup()
                .fork();

        // Load activity timeline
        final LoadPeriodActivityTimelineJob loadPeriodActivityTimelineJob =
                new LoadPeriodActivityTimelineJob(
                        new LoadPeriodActivityTimeSumJob(mDatabaseHelper),
                        mDatabaseHelper);
        loadPeriodActivityTimelineJob.setTaskLoadFilter(mTaskLoadFilter);

        ForkJoiner timelineActivityJoiner = buildFork(loadPeriodActivityTimelineJob)
                .groupOnTheSameGroup()
                .fork();

        // Load activity stacked timeline
        LoadPeriodSplitActivityTimelineJob loadPeriodSplitActivityTimelineJob =
                Injection.sJobsComponent.loadPeriodSplitActivityTimelineJob();
        loadPeriodSplitActivityTimelineJob.setTaskLoadFilter(mTaskLoadFilter);

        ForkJoiner splitTimelineJoiner = buildFork(loadPeriodSplitActivityTimelineJob)
                .groupOnTheSameGroup()
                .fork();


        List<TaskOverallActivity> taskOverallActivityTime =
                ((LoadJobResult<List<TaskOverallActivity>>) overallActivityJoiner.join()).getData();
        results.add(new OverallActivityTimePieBinder(taskOverallActivityTime));

        List<DailyActivityDuration> activityTimeline =
                ((LoadJobResult<List<DailyActivityDuration>>) timelineActivityJoiner.join()).getData();
        results.add(new ActivityTimelineBinder(activityTimeline));

        List<DailyTaskActivityDuration> activitySplitTimeline =
                ((LoadJobResult<List<DailyTaskActivityDuration>>) splitTimelineJoiner.join()).getData();
        results.add(new ActivityStackedTimelineBinder(activitySplitTimeline));



        return new LoadJobResult<>(results);
    }
}
