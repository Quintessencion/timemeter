package com.simbirsoft.timemeter.jobs;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.JobResultStatus;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.model.Period;
import com.simbirsoft.timemeter.model.TaskTimespansLoadFilter;
import com.simbirsoft.timemeter.ui.util.TimeUtils;
import com.squareup.phrase.Phrase;

import java.util.List;

import javax.inject.Inject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadTaskTimespansJob extends LoadJob {

    private final DatabaseHelper mDatabaseHelper;
    private Long mTaskId;
    private TaskTimespansLoadFilter mFilter;

    @Inject
    public LoadTaskTimespansJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    public void setTaskId(long taskId) {
        Preconditions.checkArgument(mTaskId == null, "task id is already set");

        mTaskId = taskId;
        mFilter = new TaskTimespansLoadFilter();
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

    public TaskTimespansLoadFilter getFilter() {
        return mFilter;
    }

    @Override
    protected LoadJobResult<?> performLoad() {
        List<TaskTimeSpan> result = cupboard().withDatabase(mDatabaseHelper.getReadableDatabase())
                .query(TaskTimeSpan.class)
                .withSelection(Phrase.from("{table_tts}.{table_tts_column_task_id} = ? {filter}")
                        .put("table_tts", TaskTimeSpan.TABLE_NAME)
                        .put("table_tts_column_task_id", TaskTimeSpan.COLUMN_TASK_ID)
                        .put("filter", getFilterConditions())
                        .format()
                        .toString(), String.valueOf(mTaskId))
                .list();

        return new LoadJobResult<>(JobResultStatus.OK, result);
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
