package com.simbirsoft.timemeter.jobs;

import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.base.BaseJob;
import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.models.JobResultStatus;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTag;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.model.TaskBundle;

import org.slf4j.Logger;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class SaveTaskBundleJob extends BaseJob {

    public static class SaveTaskResult extends JobEvent {

        private long mTaskId;

        SaveTaskResult(long taskId) {
            mTaskId = taskId;

            setEventCode(EVENT_CODE_OK);
            setJobStatus(JobResultStatus.OK);
        }

        public long getTaskId() {
            return mTaskId;
        }
    }

    private static final Logger LOG = LogFactory.getLogger(SaveTaskBundleJob.class);

    private final DatabaseHelper mDatabaseHelper;
    private TaskBundle mTaskBundle;

    @Inject
    public SaveTaskBundleJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Preconditions.checkArgument(mTaskBundle != null);
        Preconditions.checkArgument(mTaskBundle.getTask() != null);
    }

    @Override
    protected JobEvent executeImpl() throws Exception {
        final Long taskId = mTaskBundle.getTask().getId();
        final Task task = mTaskBundle.getTask();

        if (!task.hasId()) {
            task.setCreateDate(new Date(System.currentTimeMillis()));
        }

        LOG.trace("saving task {}", task);
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            DatabaseCompartment cupboard = cupboard().withDatabase(db);

            if (taskId != null) {
                int count = cupboard.delete(TaskTag.class, "taskId=?", String.valueOf(taskId));
                LOG.trace("{} task {} tags removed", count, task);
            }

            cupboard.put(task);
            LOG.trace("task {} added", task);

            List<Tag> tags = mTaskBundle.getTags();
            if (tags == null) {
                tags = Lists.newArrayList();
            }
            cupboard.put(tags);
            LOG.trace("{} tags added", tags.size());

            List<TaskTag> taskTags = Lists.transform(
                    tags, (tag) -> TaskTag.create(task, tag));
            cupboard.put(taskTags);
            LOG.trace("{} task tags added for task {}", taskTags.size(), task);

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
        LOG.trace("saved task {}", task);

        return new SaveTaskResult(task.getId());
    }

    public TaskBundle getTaskBundle() {
        return mTaskBundle;
    }

    public void setTaskBundle(TaskBundle taskBundle) {
        Preconditions.checkArgument(mTaskBundle == null);

        mTaskBundle = taskBundle;
    }
}
