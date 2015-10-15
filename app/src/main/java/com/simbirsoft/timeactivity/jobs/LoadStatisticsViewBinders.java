package com.simbirsoft.timeactivity.jobs;

import android.content.Context;

import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Lists;
import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.injection.Injection;
import com.simbirsoft.timeactivity.log.LogFactory;
import com.simbirsoft.timeactivity.model.TaskLoadFilter;
import com.simbirsoft.timeactivity.model.TaskOverallActivity;
import com.simbirsoft.timeactivity.ui.model.DailyActivityDuration;
import com.simbirsoft.timeactivity.ui.model.DailyTaskActivityDuration;
import com.simbirsoft.timeactivity.ui.stats.StatisticsViewBinder;
import com.simbirsoft.timeactivity.ui.stats.binders.ActivityStackedTimelineBinder;
import com.simbirsoft.timeactivity.ui.stats.binders.ActivityTimelineBinder;
import com.simbirsoft.timeactivity.ui.stats.binders.OverallActivityTimePieBinder;

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
