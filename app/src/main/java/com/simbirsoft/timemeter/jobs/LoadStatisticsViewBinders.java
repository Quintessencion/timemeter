package com.simbirsoft.timemeter.jobs;

import android.content.Context;

import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.model.TaskLoadFilter;
import com.simbirsoft.timemeter.model.TaskOverallActivity;
import com.simbirsoft.timemeter.ui.stats.StatisticsViewBinder;
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

        LoadOverallTaskActivityTimeJob loadOverallTaskActivityTimeJob =
                new LoadOverallTaskActivityTimeJob(mContext, mDatabaseHelper);
        loadOverallTaskActivityTimeJob.setTaskLoadFilter(mTaskLoadFilter);

        ForkJoiner joiner = buildFork(loadOverallTaskActivityTimeJob)
                .groupOn(JobManager.JOB_GROUP_UNIQUE)
                .fork();

        List<TaskOverallActivity> taskOverallActivityTime =
                ((LoadJobResult<List<TaskOverallActivity>>) joiner.join()).getData();
        results.add(new OverallActivityTimePieBinder(taskOverallActivityTime));

        return new LoadJobResult<>(results);
    }
}
