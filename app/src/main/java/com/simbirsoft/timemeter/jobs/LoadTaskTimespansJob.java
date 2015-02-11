package com.simbirsoft.timemeter.jobs;

import android.util.Log;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.JobResultStatus;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.squareup.phrase.Phrase;

import java.util.List;

import javax.inject.Inject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadTaskTimespansJob extends LoadJob {

    private final DatabaseHelper mDatabaseHelper;
    private Long mTaskId;

    @Inject
    public LoadTaskTimespansJob(DatabaseHelper databaseHelper) {
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
        List<TaskTimeSpan> result = cupboard().withDatabase(mDatabaseHelper.getReadableDatabase())
                .query(TaskTimeSpan.class)
                .withSelection(Phrase.from("{table_tts}.{table_tts_column_task_id} = ?")
                        .put("table_tts", TaskTimeSpan.TABLE_NAME)
                        .put("table_tts_column_task_id", TaskTimeSpan.COLUMN_TASK_ID)
                        .format()
                        .toString(), String.valueOf(mTaskId))
                .list();

        return new LoadJobResult<>(JobResultStatus.OK, result);
    }
}
