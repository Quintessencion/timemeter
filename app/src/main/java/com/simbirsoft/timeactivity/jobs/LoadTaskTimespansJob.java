package com.simbirsoft.timeactivity.jobs;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.JobResultStatus;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Preconditions;
import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.db.model.TaskTimeSpan;
import com.simbirsoft.timeactivity.model.TaskLoadFilter;
import com.squareup.phrase.Phrase;

import java.util.List;

import javax.inject.Inject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadTaskTimespansJob extends LoadJob {

    private final DatabaseHelper mDatabaseHelper;
    private Long mTaskId;
    private TaskLoadFilter mFilter;

    @Inject
    public LoadTaskTimespansJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
        mFilter = new TaskLoadFilter();
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
        mFilter.clear();
    }

    public TaskLoadFilter getFilter() {
        return mFilter;
    }

    @Override
    protected LoadJobResult<?> performLoad() {
        List<TaskTimeSpan> result = loadSpans();
        return new LoadJobResult<>(JobResultStatus.OK, result);
    }

    public List<TaskTimeSpan> loadSpans() {
        return cupboard().withDatabase(mDatabaseHelper.getReadableDatabase())
                .query(TaskTimeSpan.class)
                .withSelection(Phrase.from("{table_tts}.{table_tts_column_task_id} = ? {filter}")
                        .put("table_tts", TaskTimeSpan.TABLE_NAME)
                        .put("table_tts_column_task_id", TaskTimeSpan.COLUMN_TASK_ID)
                        .put("filter", getFilterConditions())
                        .format()
                        .toString(), String.valueOf(mTaskId))
                .list();
    }

    private String getFilterConditions() {
        StringBuilder where = new StringBuilder();
        long[] dateBounds = new long[2];
        mFilter.getDateBounds(dateBounds);
        if (dateBounds[0] > 0) {
            where.append(Phrase.from("AND {table_tts}.{table_tts_column_end_time} > {start_time}")
                            .put("table_tts", TaskTimeSpan.TABLE_NAME)
                            .put("table_tts_column_end_time", TaskTimeSpan.COLUMN_END_TIME)
                            .put("start_time", String.valueOf(dateBounds[0])).format()
            );
            if (dateBounds[1] > 0) {
                Preconditions.checkArgument(dateBounds[1] > dateBounds[0], "end date should be greater than start date");
                where.append(Phrase.from(" AND {table_tts}.{table_tts_column_start_time} < {end_time}")
                                .put("table_tts", TaskTimeSpan.TABLE_NAME)
                                .put("table_tts_column_start_time", TaskTimeSpan.COLUMN_START_TIME)
                                .put("end_time", String.valueOf(dateBounds[1])).format()
                );
            }
        }
        return where.toString();
    }
}
