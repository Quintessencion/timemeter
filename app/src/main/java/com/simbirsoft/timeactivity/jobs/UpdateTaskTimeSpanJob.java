package com.simbirsoft.timeactivity.jobs;

import com.be.android.library.worker.base.BaseJob;
import com.be.android.library.worker.models.JobResultStatus;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.db.model.TaskTimeSpan;
import com.squareup.phrase.Phrase;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class UpdateTaskTimeSpanJob extends BaseJob {

    public static final int ERROR_BAD_RANGE = -1;
    public static final int ERROR_BELONGS_TO_FUTURE = -2;
    public static final int ERROR_OVERLAPS = -3;

    private DatabaseHelper mDatabaseHelper;
    private TaskTimeSpan mSpan;

    @Inject
    public UpdateTaskTimeSpanJob(DatabaseHelper dbHelper) {
        mDatabaseHelper = dbHelper;
    }

    public void setSpan(TaskTimeSpan span) {
        mSpan = span;
    }

    @Override
    protected LoadJobResult<TaskTimeSpan> executeImpl() throws Exception {
        if (mSpan.getStartTimeMillis() >= mSpan.getEndTimeMillis()) {
            return new LoadJobResult<>(ERROR_BAD_RANGE, JobResultStatus.FAILED, null);
        }

        Calendar c = Calendar.getInstance();
        long millisNow = c.getTimeInMillis();
        if (mSpan.getStartTimeMillis() > millisNow || mSpan.getEndTimeMillis() > millisNow) {
            return new LoadJobResult<>(ERROR_BELONGS_TO_FUTURE, JobResultStatus.FAILED, null);
        }

        DatabaseCompartment db = cupboard().withDatabase(mDatabaseHelper.getWritableDatabase());
        List<TaskTimeSpan> overlaps = db.query(TaskTimeSpan.class).withSelection(getOverlapClause(mSpan)).list();
        boolean noOverlaps = overlaps.isEmpty() || (overlaps.size() == 1 && overlaps.get(0).getId().equals(mSpan.getId()));
        if (noOverlaps) {
            db.put(mSpan);
            return new LoadJobResult<TaskTimeSpan>(mSpan);
        } else {
            return new LoadJobResult(ERROR_OVERLAPS, JobResultStatus.FAILED, null);
        }
    }

    private String getOverlapClause(TaskTimeSpan span) {
        return Phrase.from("{start_time} <= {table_tts}.{table_tts_column_end_time} AND {table_tts}.{table_tts_column_start_time} <= {end_time}")
                .put("table_tts", TaskTimeSpan.TABLE_NAME)
                .put("table_tts_column_start_time", TaskTimeSpan.COLUMN_START_TIME)
                .put("table_tts_column_end_time", TaskTimeSpan.COLUMN_END_TIME)
                .put("start_time", String.valueOf(span.getStartTimeMillis()))
                .put("end_time", String.valueOf(span.getEndTimeMillis()))
                .format()
                .toString();
    }
}
