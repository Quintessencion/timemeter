package com.simbirsoft.timemeter.jobs;

import android.content.Context;

import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.model.TaskLoadFilter;
import com.simbirsoft.timemeter.model.TaskOverallActivity;
import com.simbirsoft.timemeter.ui.model.DailyActivityDuration;
import com.simbirsoft.timemeter.ui.model.DailyTaskActivityDuration;
import com.simbirsoft.timemeter.ui.stats.StatisticsViewBinder;
import com.simbirsoft.timemeter.ui.stats.binders.ActivityStackedTimelineBinder;
import com.simbirsoft.timemeter.ui.stats.binders.ActivityTimelineBinder;
import com.simbirsoft.timemeter.ui.stats.binders.OverallActivityTimePieBinder;

import org.slf4j.Logger;

import java.util.List;

import javax.inject.Inject;

public class LoadStatisticsViewBinders extends LoadJob implements FilterableJob {

    private static final Logger LOG = LogFactory.getLogger(LoadStatisticsViewBinders.class);

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
                .groupOn(JobManager.JOB_GROUP_UNIQUE)
                .fork();

        // Load activity timeline
        final LoadPeriodActivityTimelineJob loadPeriodActivityTimelineJob =
                new LoadPeriodActivityTimelineJob(
                        new LoadPeriodActivityTimeSumJob(mDatabaseHelper),
                        mDatabaseHelper);
        loadPeriodActivityTimelineJob.setTaskLoadFilter(mTaskLoadFilter);

        ForkJoiner timelineActivityJoiner = buildFork(loadPeriodActivityTimelineJob)
                .groupOn(JobManager.JOB_GROUP_UNIQUE)
                .fork();

        // Load activity stacked timeline
        LoadPeriodSplitActivityTimelineJob loadPeriodSplitActivityTimelineJob =
                Injection.sJobsComponent.loadPeriodSplitActivityTimelineJob();
        loadPeriodSplitActivityTimelineJob.setTaskLoadFilter(mTaskLoadFilter);

        ForkJoiner splitTimelineJoiner = buildFork(loadPeriodSplitActivityTimelineJob)
                .groupOn(JobManager.JOB_GROUP_UNIQUE)
                .fork();

        if (isCancelled()) {
            LOG.trace("cancelled");
            loadOverallTaskActivityTimeJob.cancel();
            loadPeriodActivityTimelineJob.cancel();
            loadPeriodSplitActivityTimelineJob.cancel();

            return LoadJobResult.loadOk();
        }

        List<TaskOverallActivity> taskOverallActivityTime =
                ((LoadJobResult<List<TaskOverallActivity>>) overallActivityJoiner.join()).getData();
        results.add(new OverallActivityTimePieBinder(taskOverallActivityTime));

        if (isCancelled()) {
            LOG.trace("cancelled");
            loadPeriodActivityTimelineJob.cancel();
            loadPeriodSplitActivityTimelineJob.cancel();

            return LoadJobResult.loadOk();
        }

        List<DailyActivityDuration> activityTimeline =
                ((LoadJobResult<List<DailyActivityDuration>>) timelineActivityJoiner.join()).getData();
        results.add(new ActivityTimelineBinder(activityTimeline));

        if (isCancelled()) {
            LOG.trace("cancelled");
            loadPeriodSplitActivityTimelineJob.cancel();

            return LoadJobResult.loadOk();
        }

        List<DailyTaskActivityDuration> activitySplitTimeline =
                ((LoadJobResult<List<DailyTaskActivityDuration>>) splitTimelineJoiner.join()).getData();
        results.add(new ActivityStackedTimelineBinder(activitySplitTimeline));

        return new LoadJobResult<>(results);
    }
}
