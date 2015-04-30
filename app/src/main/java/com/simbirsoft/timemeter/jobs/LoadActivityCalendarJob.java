package com.simbirsoft.timemeter.jobs;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.QueryUtils;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.model.Period;
import com.simbirsoft.timemeter.model.TaskLoadFilter;
import com.simbirsoft.timemeter.ui.model.ActivityCalendar;
import com.simbirsoft.timemeter.ui.model.CalendarData;
import com.simbirsoft.timemeter.ui.model.CalendarPeriod;
import com.simbirsoft.timemeter.ui.util.TimeUtils;
import com.squareup.phrase.Phrase;

import org.slf4j.Logger;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadActivityCalendarJob extends LoadJob implements FilterableJob {

    private static final Logger LOG = LogFactory.getLogger(LoadActivityCalendarJob.class);

    private final DatabaseHelper mDatabaseHelper;
    private TaskLoadFilter mLoadFilter;
    private Date mStartDate;
    private Date mEndDate;
    private long mPrevFilterDateMillis;
    private long mPeriodStartMillis;
    private long mPeriodEndMillis;

    @Inject
    public LoadActivityCalendarJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
        mLoadFilter = new TaskLoadFilter();
    }

    @Override
    protected LoadJobResult<CalendarData> performLoad() throws Exception {
        mPeriodStartMillis = mPeriodEndMillis = 0;
        adjustDates();

        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        final long startDateMillis = mStartDate.getTime();
        final long endDateMillis = TimeUtils.getDayEndMillis(mEndDate.getTime());

        final long filterDateMillis = mLoadFilter.getDateMillis();
        final Collection<Tag> filterTags = mLoadFilter.getFilterTags();

        StringBuilder where = new StringBuilder();
        StringBuilder join = new StringBuilder();

        Phrase queryPhrase = Phrase.from(
                "SELECT {table_tts}.* " +
                        "FROM {table_tts} {join} " +
                        "WHERE {table_tts}.{table_tts_column_start_time} < {end_date} " +
                        "AND {table_tts}.{table_tts_column_end_time} > {start_date} {and_where} " +
                        "ORDER BY {table_tts}.{table_tts_column_start_time}")
                .put("table_tts", TaskTimeSpan.TABLE_NAME)
                .put("table_tts_column_start_time", TaskTimeSpan.COLUMN_START_TIME)
                .put("table_tts_column_end_time", TaskTimeSpan.COLUMN_END_TIME)
                .put("start_date", String.valueOf(startDateMillis))
                .put("end_date", String.valueOf(endDateMillis));

        if (!TextUtils.isEmpty(mLoadFilter.getSearchText())) {
            where.append(Phrase.from(" AND {table_task}.{table_task_description} LIKE '%{search_text}%'")
                    .put("table_task", Task.TABLE_NAME)
                    .put("table_task_description", Task.COLUMN_DESCRIPTION)
                    .put("search_text", mLoadFilter.getSearchText())
                    .format());
            join.append(Phrase.from("INNER JOIN {table_task} " +
                    "ON {table_tts}.{table_tts_column_task_id}={table_task}.{table_task_column_id}")
                    .put("table_task", Task.TABLE_NAME)
                    .put("table_task_column_id", Task.COLUMN_ID)
                    .put("table_tts", TaskTimeSpan.TABLE_NAME)
                    .put("table_tts_column_task_id", TaskTimeSpan.COLUMN_TASK_ID)
                    .format());
        }

        if (!filterTags.isEmpty()) {
            where.append(" AND ");
            where.append(QueryUtils.createTagsRestrictionStatementForTimeSpan(filterTags));
        }

        queryPhrase = queryPhrase.put("and_where", where);
        queryPhrase = queryPhrase.put("join", join);

        final String query = queryPhrase.format().toString();

        if (isCancelled()) {
            LOG.trace("cancelled");
            return LoadJobResult.loadOk();
        }

        ActivityCalendar calendar = new ActivityCalendar();
        CalendarPeriod period = new CalendarPeriod();
        Cursor cursor = db.rawQuery(query, new String[0]);
        try {
            List<TaskTimeSpan> spans = cupboard().withCursor(cursor).list(TaskTimeSpan.class);
            calendar.setStartDate(mStartDate);
            calendar.setEndDate(mEndDate);
            calendar.setDailyActivity(spans);
            period.setFilterDateMillis(filterDateMillis);
            period.setPeriodStartMillis(mPeriodStartMillis);
            period.setPeriodEndMillis(mPeriodEndMillis);
            period.setStartDate(mStartDate);
            period.setEndDate(mEndDate);
            return new LoadJobResult<>(new CalendarData(calendar, period));
        } finally {
            cursor.close();
        }
    }

    @Override
    public TaskLoadFilter getTaskLoadFilter() {
        return mLoadFilter;
    }

    @Override
    public void setTaskLoadFilter(TaskLoadFilter filter) {
        mLoadFilter = filter;
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date startDate) {
        mStartDate = startDate;
    }

    public Date getEndDate() {
        return mEndDate;
    }

    public void setEndDate(Date endDate) {
        mEndDate = endDate;
    }

    public long getPrevFilterDateMillis() {
        return mPrevFilterDateMillis;
    }

    public void setPrevFilterDateMillis(long millis) {
        mPrevFilterDateMillis = millis;
    }

    /**
     * Checks if need to recalculate weed start and end dates (mStartDate and mEndDate)
     * Calculates week start and end dates
     * Calculates calendar period start and end dates (mPeriodStartMillis and mPeriodEndMillis)
     **/
    private void adjustDates() {
        final long filterDateMillis = mLoadFilter.getDateMillis();
        final Period filterPeriod = mLoadFilter.getPeriod();
        if (filterDateMillis != mPrevFilterDateMillis) {
            mStartDate = null;
            mEndDate = null;
        }
        calculatePeriodStartEnd(filterDateMillis, filterPeriod);
        if (mStartDate != null && mEndDate != null) return;
        long startDateMillis = (mPeriodStartMillis > 0)
                ? mPeriodStartMillis
                : TimeUtils.getWeekFirstDayStartMillis(new Date().getTime());
        mStartDate = new Date(startDateMillis);
        mEndDate = new Date(TimeUtils.getWeekLastDayStartMillis(startDateMillis));
    }

    /**
     * Calculates calendar period start and end dates by TaskLoadFilter parameters
     * @param filterDateMillis - period start date in milliseconds
     * @param filterPeriod - period duration
     */
    private void calculatePeriodStartEnd(long filterDateMillis, Period filterPeriod) {
        if (filterDateMillis == 0) return;
        mPeriodStartMillis = TimeUtils.getWeekFirstDayStartMillis(filterDateMillis);
        if (filterPeriod == null) return;
        long filterPeriodEnd = Period.getPeriodEnd(filterPeriod, filterDateMillis);
        if (filterPeriodEnd == 0) return;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(filterPeriodEnd);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        mPeriodEndMillis = TimeUtils.getWeekLastDayStartMillis(calendar);
        if (mEndDate != null && mEndDate.getTime() > mPeriodEndMillis) {
            mStartDate = null;
            mEndDate = null;
        }
    }

}
