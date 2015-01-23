package com.simbirsoft.timemeter.jobs;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.JobResultStatus;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Task;

import java.util.List;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

import javax.inject.Inject;

public class LoadTaskListJob extends LoadJob {

    private final DatabaseHelper mDatabaseHelper;

    @Inject
    public LoadTaskListJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    @Override
    protected LoadJobResult<?> performLoad() {
        List<Task> tasks = cupboard()
                .withDatabase(mDatabaseHelper.getWritableDatabase())
                .query(Task.class)
                .list();

        return new LoadJobResult<>(JobResultStatus.OK, tasks);
    }
}
