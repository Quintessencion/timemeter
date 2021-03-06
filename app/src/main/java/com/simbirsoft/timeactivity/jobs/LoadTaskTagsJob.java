package com.simbirsoft.timeactivity.jobs;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.JobResultStatus;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Preconditions;
import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.db.QueryUtils;
import com.simbirsoft.timeactivity.db.model.Tag;

import java.util.List;

import javax.inject.Inject;

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
        List<Tag> tags = QueryUtils.getTagsForTask(mDatabaseHelper.getWritableDatabase(), mTaskId);
        return new LoadJobResult<>(JobResultStatus.OK, tags);
    }
}
