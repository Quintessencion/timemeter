package com.simbirsoft.timemeter.jobs;

import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.exceptions.JobExecutionException;
import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.JobResultStatus;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.ui.model.TaskBundle;

import java.util.List;

import javax.inject.Inject;

import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadTaskListJob extends LoadJob {

    private final DatabaseHelper mDatabaseHelper;

    @Inject
    public LoadTaskListJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;

        setGroupId(JobManager.JOB_GROUP_UNIQUE);
    }

    @Override
    protected LoadJobResult<?> performLoad() throws JobExecutionException {
        DatabaseCompartment cupboard = cupboard()
                .withDatabase(mDatabaseHelper.getWritableDatabase());

        final List<Task> tasks = cupboard.query(Task.class)
                .orderBy(Task.COLUMN_CREATE_DATE + " DESC")
                .list();

        final List<TaskBundle> result = Lists.newArrayListWithCapacity(tasks.size());
        final LoadTaskTagsJob loadJob = Injection.sJobsComponent.loadTaskTagsJob();

        for (Task task : tasks) {
            loadJob.setTaskId(task.getId());
            List<Tag> taskTags = ((LoadJobResult<List<Tag>>) forkJob(loadJob).join()).getData();
            result.add(TaskBundle.create(task, taskTags));
            loadJob.reset();
        }

        return new LoadJobResult<>(JobResultStatus.OK, result);
    }
}
