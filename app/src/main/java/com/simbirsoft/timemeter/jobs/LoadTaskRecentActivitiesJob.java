package com.simbirsoft.timemeter.jobs;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.model.Period;
import com.simbirsoft.timemeter.ui.model.TaskRecentActivity;
import com.simbirsoft.timemeter.ui.util.TimeSpanDaysSplitter;
import com.squareup.phrase.Phrase;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class LoadTaskRecentActivitiesJob extends LoadJob{
    private static final int DAYS_COUNT = 14;

    private final DatabaseHelper mDatabaseHelper;
    private final LoadTaskTimespansJob mLoadSpansJob;
    private Long mTaskId;
    private List<TaskTimeSpan> mTaskTimeSpans;

    @Inject
    public LoadTaskRecentActivitiesJob(DatabaseHelper databaseHelper, LoadTaskTimespansJob loadSpansJob) {
        mDatabaseHelper = databaseHelper;
        mLoadSpansJob = loadSpansJob;
    }

    public void setTaskId(long taskId) {
        mTaskId = taskId;
    }

    public void setTaskTimeSpans(List<TaskTimeSpan> spans) {
        mTaskTimeSpans = spans;
    }

    @Override
    protected LoadJobResult<TaskRecentActivity> performLoad() throws Exception {
        TaskRecentActivity recentActivity = new TaskRecentActivity();
        long startTime;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, -DAYS_COUNT);
        startTime = cal.getTimeInMillis();
        if (mTaskTimeSpans != null && !mTaskTimeSpans.isEmpty()) {
            Collections.sort(mTaskTimeSpans, (item1, item2) ->
                    (int) (item1.getStartTimeMillis() - item2.getStartTimeMillis()));
            startTime = Math.min(mTaskTimeSpans.get(0).getStartTimeMillis(), startTime);
        }
        mLoadSpansJob.getFilter().startDateMillis(startTime)
                .period(Period.ALL);
        mLoadSpansJob.setTaskId(mTaskId);
        List<TaskTimeSpan> spans =
                ((LoadJobResult<List<TaskTimeSpan>>) forkJob(mLoadSpansJob).join()).getData();
        if (!spans.isEmpty()) {
            recentActivity.setList(TimeSpanDaysSplitter.convertToTaskRecentActivityItems(spans));
        } else {
            recentActivity.setRecentActivityTime(getRecentActivityTime());
        }
        return new LoadJobResult<>(recentActivity);
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
