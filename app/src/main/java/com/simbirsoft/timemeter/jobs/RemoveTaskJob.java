package com.simbirsoft.timemeter.jobs;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.be.android.library.worker.base.BaseJob;
import com.be.android.library.worker.base.JobEvent;
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.controller.ITaskActivityManager;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTag;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.injection.ApplicationModule;
import com.simbirsoft.timemeter.log.LogFactory;
import com.squareup.phrase.Phrase;

import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class RemoveTaskJob extends BaseJob {

    private static final Logger LOG = LogFactory.getLogger(RemoveTaskJob.class);

    private final DatabaseHelper mDatabaseHelper;
    private final ITaskActivityManager mITaskActivityManager;
    private Task mTask;
    private Handler mHandler;

    @Inject
    public RemoveTaskJob(DatabaseHelper databaseHelper, ITaskActivityManager iTaskActivityManager, @Named(ApplicationModule.HANDLER_MAIN) Handler handler) {
        mDatabaseHelper = databaseHelper;
        mITaskActivityManager = iTaskActivityManager;
        mHandler = handler;
    }

    @Override
    protected void onPreExecute() throws Exception {
        super.onPreExecute();

        Preconditions.checkArgument(mTask != null);
        Preconditions.checkArgument(mITaskActivityManager != null);

    }

    public void setTask(Task task) {
        Preconditions.checkArgument(mTask == null);

        mTask = task;
    }

    @Override
    protected JobEvent executeImpl() throws Exception {
        LOG.trace("removing task id:'{}'", mTask.getId());
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            mHandler.post(() -> mITaskActivityManager.stopTask(mTask));

            DatabaseCompartment cupboard = cupboard().withDatabase(db);

            int count = cupboard.delete(
                    TaskTag.class,
                    Phrase.from("{task_id}=?")
                            .put("task_id", TaskTag.COLUMN_TASK_ID)
                            .format()
                            .toString(),
                    String.valueOf(mTask.getId()));
            LOG.trace("'{}' task id:'{}' tags removed", count, mTask.getId());

            count = cupboard.delete(TaskTimeSpan.class,
                    Phrase.from("{task_id}=?")
                            .put("task_id", TaskTimeSpan.COLUMN_TASK_ID)
                            .format()
                            .toString(),
                    String.valueOf(mTask.getId()));
            LOG.trace("{} task id:'{}' spans removed", count, mTask.getId());

            cupboard.delete(Task.class, mTask.getId());
            LOG.trace("task id:'{}' removed", mTask.getId());

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }

        return JobEvent.ok();
    }
}
