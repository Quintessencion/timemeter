package com.simbirsoft.timemeter.jobs;

import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.base.BaseJob;
import com.be.android.library.worker.base.JobEvent;
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTag;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.log.LogFactory;
import com.squareup.phrase.Phrase;

import org.slf4j.Logger;

import java.util.List;

import javax.inject.Inject;

import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class RemoveTaskJob extends BaseJob {

    private static final Logger LOG = LogFactory.getLogger(RemoveTaskJob.class);

    private final DatabaseHelper mDatabaseHelper;
    private Long mTaskId;

    @Inject
    public RemoveTaskJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    @Override
    protected void onPreExecute() throws Exception {
        super.onPreExecute();

        Preconditions.checkArgument(mTaskId != null);
    }

    public void setTaskId(long taskId) {
        Preconditions.checkArgument(mTaskId == null);

        mTaskId = taskId;
    }

    @Override
    protected JobEvent executeImpl() throws Exception {
        LOG.trace("removing task id:'{}'", mTaskId);
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            DatabaseCompartment cupboard = cupboard().withDatabase(db);

            int count = cupboard.delete(
                    TaskTag.class,
                    Phrase.from("{task_id}=?")
                            .put("task_id", TaskTag.COLUMN_TASK_ID)
                            .format()
                            .toString(),
                    String.valueOf(mTaskId));
            LOG.trace("'{}' task id:'{}' tags removed", count, mTaskId);

            count = cupboard.delete(TaskTimeSpan.class,
                    Phrase.from("{task_id}=?")
                            .put("task_id", TaskTimeSpan.COLUMN_TASK_ID)
                            .format()
                            .toString(),
                    String.valueOf(mTaskId));
            LOG.trace("{} task id:'{}' spans removed", count, mTaskId);

            cupboard.delete(Task.class, mTaskId);
            LOG.trace("task id:'{}' removed", mTaskId);

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }

        return JobEvent.ok();
    }
}
