package com.simbirsoft.timemeter.jobs;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;

import javax.inject.Inject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadTaskTimeSpanJob extends LoadJob {
    private final DatabaseHelper mDatabaseHelper;
    private Long mTaskTimeSpanId;

    @Inject
    public LoadTaskTimeSpanJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    public void setTaskTimeSpanId(long taskTimeSpanId) {
        mTaskTimeSpanId = taskTimeSpanId;
    }

    @Override
    protected void onPreExecute() throws Exception {
        super.onPreExecute();

        Preconditions.checkArgument(mTaskTimeSpanId != null, "one should specify span id");
    }

    @Override
    protected LoadJobResult<?> performLoad() throws Exception {
        TaskTimeSpan span = cupboard().withDatabase(mDatabaseHelper.getWritableDatabase())
                .query(TaskTimeSpan.class)
                .byId(mTaskTimeSpanId)
                .get();

        if (span == null) {
            return new LoadJobResult<>((TaskTimeSpan) null);
        }

        return new LoadJobResult<>(span);
    }
}
