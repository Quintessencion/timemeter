package com.simbirsoft.timemeter.jobs;


import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.model.TaskTimespansLoadFilter;
import com.simbirsoft.timemeter.ui.model.TaskActivityItem;
import com.simbirsoft.timemeter.ui.util.TimeSpanDaysSplitter;

import java.util.List;

import javax.inject.Inject;

public class LoadTaskActivitiesJob extends LoadJob {

    private final LoadTaskTimespansJob mLoadSpansJob;

    @Inject
    public LoadTaskActivitiesJob(LoadTaskTimespansJob loadSpansJob) {
        mLoadSpansJob = loadSpansJob;
    }

    public void setTaskId(long taskId) {
        mLoadSpansJob.setTaskId(taskId);
    }

    public TaskTimespansLoadFilter getFilter() {
        return mLoadSpansJob.getFilter();
    }

    @Override
    protected LoadJobResult<List<TaskActivityItem>> performLoad() throws Exception {
        List<TaskTimeSpan> spans =
                ((LoadJobResult<List<TaskTimeSpan>>) forkJob(mLoadSpansJob).join()).getData();
        long[] dateBounds = new long[2];
        mLoadSpansJob.getFilter().getDateBounds(dateBounds);
        return new LoadJobResult<>(TimeSpanDaysSplitter.convertToTaskActivityItems(spans,
                dateBounds[0], dateBounds[1]));
    }
}
