package com.simbirsoft.timemeter.jobs;

import android.database.Cursor;
import android.util.Log;

import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.JobResultStatus;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTag;
import com.squareup.phrase.Phrase;

import java.util.List;

import javax.inject.Inject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadTaskTagsJob extends LoadJob {

    private final DatabaseHelper mDatabaseHelper;
    private Long mTaskId;

    @Inject
    public LoadTaskTagsJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    public void setTaskId(long taskId) {
        Preconditions.checkArgument(mTaskId == null, "task id is already set");

        mTaskId = taskId;
    }

    @Override
    protected void onPreExecute() throws Exception {
        super.onPreExecute();

        Preconditions.checkArgument(mTaskId != null, "one should specify task id");
    }

    @Override
    public void onReset() {
        super.onReset();

        mTaskId = null;
    }

    @Override
    protected LoadJobResult<?> performLoad() {
        final String query = Phrase.from(
                "select * " +
                "from {table_tag} " +
                "where {table_tag}.{table_tag_column_tag_id} in " +
                        "(select {table_tasktag}.{table_tasktag_column_tag_id} " +
                        "from {table_tasktag} " +
                        "where {table_tasktag}.{table_tasktag_column_task_id}=?)")
                .put("table_tag", Tag.TABLE_NAME)
                .put("table_tag_column_tag_id", Tag.COLUMN_ID)
                .put("table_tasktag", TaskTag.TABLE_NAME)
                .put("table_tasktag_column_tag_id", TaskTag.COLUMN_TAG_ID)
                .put("table_tasktag_column_task_id", TaskTag.COLUMN_TASK_ID)
                .format()
                .toString();

        String[] args = new String[]{mTaskId.toString()};
        Cursor c = mDatabaseHelper.getWritableDatabase().rawQuery(query, args);

        try {
            List<Tag> tags = cupboard().withCursor(c).list(Tag.class);

            return new LoadJobResult<>(JobResultStatus.OK, tags);

        } finally {
            c.close();
        }
    }
}
