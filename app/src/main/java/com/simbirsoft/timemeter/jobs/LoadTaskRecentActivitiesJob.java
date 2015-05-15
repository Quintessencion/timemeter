package com.simbirsoft.timemeter.jobs;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.ui.model.TaskRecentActivity;
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
        mDatabaseHelper = databaseHelper;
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
    protected LoadJobResult<TaskRecentActivity> performLoad() throws Exception {
        TaskRecentActivity recentActivity = new TaskRecentActivity();
        List<TaskTimeSpan> spans = getSpans();
        if (!spans.isEmpty()) {
            recentActivity.setList(TimeSpanDaysSplitter.convertToTaskRecentActivityItems(spans));
        } else {
            recentActivity.setRecentActivityTime(getRecentActivityTime());
        }
        return new LoadJobResult<>(recentActivity);
    }

    private List<TaskTimeSpan> getSpans() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.setTimeInMillis(TimeUtils.getDayStartMillis(cal));
        cal.add(Calendar.DAY_OF_YEAR, -DAYS_COUNT);
        List<TaskTimeSpan> spans = cupboard().withDatabase(mDatabaseHelper.getReadableDatabase())
                .query(TaskTimeSpan.class)
                .withSelection(Phrase.from("{table_tts}.{table_tts_column_task_id} = {task_id} " +
                        "AND {table_tts}.{table_tts_column_start_time} > {start_time}")
                        .put("table_tts", TaskTimeSpan.TABLE_NAME)
                        .put("table_tts_column_task_id", TaskTimeSpan.COLUMN_TASK_ID)
                        .put("table_tts_column_start_time", TaskTimeSpan.COLUMN_START_TIME)
                        .put("task_id", mTaskId.toString())
                        .put("start_time", String.valueOf(cal.getTimeInMillis()))
                        .format()
                        .toString())
                .list();
        return spans;
    }

    private long getRecentActivityTime() throws Exception {
        Phrase queryPhrase = Phrase.from(
                "SELECT IFNULL(max({table_tts_column_end_time}),0) " +
                        "FROM {table_tts}  " +
                        "WHERE {table_tts_column_task_id} = {task_id}")
                .put("table_tts", TaskTimeSpan.TABLE_NAME)
                .put("table_tts_column_task_id", TaskTimeSpan.COLUMN_TASK_ID)
                .put("table_tts_column_end_time", TaskTimeSpan.COLUMN_END_TIME)
                .put("task_id", mTaskId.toString());
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        Cursor c = db.rawQuery(queryPhrase.format().toString(), null);
        try {
            c.moveToFirst();
            return c.getLong(0);
        } finally {
            c.close();
        }
    }
}
