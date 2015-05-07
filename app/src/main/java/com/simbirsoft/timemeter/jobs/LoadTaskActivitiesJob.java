package com.simbirsoft.timemeter.jobs;


import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.ui.model.TaskActivityItem;
import com.simbirsoft.timemeter.ui.util.TimeSpanDaysSplitter;

import java.util.List;

import javax.inject.Inject;

public class LoadTaskActivitiesJob extends LoadJob {

    private final DatabaseHelper mDatabaseHelper;
    private final LoadTaskTimespansJob mLoadSpansJob;

    @Inject
    public LoadTaskActivitiesJob(DatabaseHelper databaseHelper, LoadTaskTimespansJob loadSpansJob) {
        mDatabaseHelper = databaseHelper;
        mLoadSpansJob = loadSpansJob;
    }

    public void setTaskId(long taskId) {
        mLoadSpansJob.setTaskId(taskId);
    }

    @Override
    protected LoadJobResult<List<TaskActivityItem>> performLoad() throws Exception {
        List<TaskTimeSpan> spans =
                ((LoadJobResult<List<TaskTimeSpan>>) forkJob(mLoadSpansJob).join()).getData();
        return new LoadJobResult<>(TimeSpanDaysSplitter.convertToTaskActivityItems(spans, true, false));
    }
}
