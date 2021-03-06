package com.simbirsoft.timeactivity.jobs;

import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.base.BaseJob;
import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.models.JobResultStatus;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.db.model.Tag;
import com.simbirsoft.timeactivity.db.model.Task;
import com.simbirsoft.timeactivity.db.model.TaskTag;
import com.simbirsoft.timeactivity.db.model.TaskTimeSpan;
import com.simbirsoft.timeactivity.log.LogFactory;
import com.simbirsoft.timeactivity.ui.model.TaskBundle;
import com.squareup.phrase.Phrase;

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
    protected void onPreExecute() throws Exception {
        super.onPreExecute();

        Preconditions.checkArgument(mTaskBundle != null);
        Preconditions.checkArgument(mTaskBundle.getTask() != null);
    }

    @Override
    protected JobEvent executeImpl() throws Exception {
        long id = saveTaskBundle(mTaskBundle);
        return new SaveTaskResult(id);
    }

    public TaskBundle getTaskBundle() {
        return mTaskBundle;
    }

    public void setTaskBundle(TaskBundle taskBundle) {
        Preconditions.checkArgument(mTaskBundle == null);

        mTaskBundle = taskBundle;
    }

    public long saveTaskBundle(TaskBundle taskBundle) {
        final Long taskId = taskBundle.getTask().getId();
        final Task task = taskBundle.getTask();

        if (!task.hasId()) {
            task.setCreateDate(new Date(System.currentTimeMillis()));
        }

        LOG.trace("saving task {}", task);
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            DatabaseCompartment cupboard = cupboard().withDatabase(db);

            if (taskId != null) {
                int count = cupboard.delete(TaskTag.class,
                        Phrase.from("{task_id}=?")
                                .put("task_id", TaskTag.COLUMN_TASK_ID)
                                .format()
                                .toString(),
                        String.valueOf(taskId));
                LOG.trace("{} task '{}' tag mappings removed", count, task.getDescription());

                count = cupboard.delete(TaskTimeSpan.class,
                        Phrase.from("{task_id}=?")
                                .put("task_id", TaskTimeSpan.COLUMN_TASK_ID)
                                .format()
                                .toString(),
                        String.valueOf(taskId));
                LOG.trace("{} task '{}' spans removed", count, task.getDescription());
            }

            cupboard.put(task);
            LOG.trace("task '{}' added", task);

            List<Tag> tags = taskBundle.getTags();
            if (tags == null) {
                tags = Lists.newArrayList();
            }
            cupboard.put(tags);
            LOG.trace("{} tags added", tags.size());

            List<TaskTag> taskTags = Lists.transform(
                    tags, (tag) -> TaskTag.create(task, tag));
            cupboard.put(taskTags);
            LOG.trace("{} task tags added for task '{}'", taskTags.size(), task.getDescription());

            List<TaskTimeSpan> spans = taskBundle.getTaskTimeSpans();
            if (!spans.isEmpty()) {
                cupboard.put(spans);
                LOG.trace("{} task spans added for task '{}'", spans.size(), task.getDescription());
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
        LOG.trace("saved task {}", task);

        return task.getId();
    }
}
