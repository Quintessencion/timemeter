package com.simbirsoft.timemeter.jobs;

import android.database.Cursor;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.JobResultStatus;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;

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
        mTaskId = taskId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Preconditions.checkArgument(mTaskId != null, "one should specify task id");
    }

    @Override
    protected LoadJobResult<?> performLoad() {
        final String query =
                "select * " +
                "from tag " +
                "where tag._id in " +
                        "(select tasktag.tagId " +
                        "from tasktag " +
                        "where tasktag.taskId=?)";

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
