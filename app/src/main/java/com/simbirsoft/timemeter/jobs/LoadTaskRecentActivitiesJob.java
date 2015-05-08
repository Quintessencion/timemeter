package com.simbirsoft.timemeter.jobs;

import android.database.Cursor;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.JobResultStatus;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.ui.model.TaskActivityItem;
import com.simbirsoft.timemeter.ui.util.TimeSpanDaysSplitter;
import com.simbirsoft.timemeter.ui.util.TimeUtils;
import com.squareup.phrase.Phrase;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadTaskRecentActivitiesJob extends LoadJob{
    private static final int DAYS_COUNT = 14;

    private final DatabaseHelper mDatabaseHelper;
    private Long mTaskId;

    @Inject
    public LoadTaskRecentActivitiesJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;;
    }

    public void setTaskId(long taskId) {
        mTaskId = taskId;
    }

    @Override
    protected void onPreExecute() throws Exception {
        super.onPreExecute();
        Preconditions.checkArgument(mTaskId != null, "one should specify task id");
    }

    @Override
    protected LoadJobResult<List<TaskActivityItem>> performLoad() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.setTimeInMillis(TimeUtils.getDayStartMillis(cal));
        cal.add(Calendar.DAY_OF_YEAR, -DAYS_COUNT);
        List<TaskTimeSpan> spans = cupboard().withDatabase(mDatabaseHelper.getReadableDatabase())
                .query(TaskTimeSpan.class)
                .withSelection(Phrase.from("{table_tts}.{table_tts_column_task_id} = {task_id} " +
                        "and {table_tts}.{table_tts_column_start_time} > {start_time}")
                        .put("table_tts", TaskTimeSpan.TABLE_NAME)
                        .put("table_tts_column_task_id", TaskTimeSpan.COLUMN_TASK_ID)
                        .put("table_tts_column_start_time", TaskTimeSpan.COLUMN_START_TIME)
                        .put("task_id", mTaskId.toString())
                        .put("start_time", String.valueOf(cal.getTimeInMillis()))
                        .format()
                        .toString())
                .list();
        return new LoadJobResult<>(TimeSpanDaysSplitter.convertToTaskRecentActivityItems(spans));
    }
}
