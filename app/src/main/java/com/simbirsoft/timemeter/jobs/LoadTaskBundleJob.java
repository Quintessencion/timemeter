package com.simbirsoft.timemeter.jobs;

import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.ui.model.TaskBundle;

import java.util.List;

import javax.inject.Inject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadTaskBundleJob extends LoadJob {

    private final DatabaseHelper mDatabaseHelper;
    private final LoadTaskTagsJob mLoadTaskTagsJob;
    private Long mTaskId;

    @Inject
    public LoadTaskBundleJob(DatabaseHelper databaseHelper, LoadTaskTagsJob loadTaskTagsJob) {
        mDatabaseHelper = databaseHelper;
        mLoadTaskTagsJob = loadTaskTagsJob;
        setGroupId(JobManager.JOB_GROUP_UNIQUE);
    }

    public void setTaskId(long taskId) {
        mTaskId = taskId;
    }

    @Override
    protected void onPreExecute() throws Exception {
        super.onPreExecute();

        Preconditions.checkArgument(mTaskId != null, "one should specify task id");
    }

    @Override
    protected LoadJobResult<?> performLoad() throws Exception {
        Task task = cupboard().withDatabase(mDatabaseHelper.getWritableDatabase())
                .query(Task.class)
                .byId(mTaskId)
                .get();

        if (task == null) {
            return new LoadJobResult<>((TaskBundle) null);
        }

        mLoadTaskTagsJob.setTaskId(mTaskId);

        final JobEvent loadResult = buildFork(mLoadTaskTagsJob)
                .groupOnTheSameGroup()
                .fork()
                .join();

        List<Tag> tags = ((LoadJobResult<List<Tag>>) loadResult).getData();

        return new LoadJobResult<>(TaskBundle.create(task, tags));
    }
}
