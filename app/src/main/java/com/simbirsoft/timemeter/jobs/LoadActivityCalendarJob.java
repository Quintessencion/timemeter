package com.simbirsoft.timemeter.jobs;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.QueryUtils;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.model.Period;
import com.simbirsoft.timemeter.model.TaskLoadFilter;
import com.simbirsoft.timemeter.model.TaskOverallActivity;
import com.simbirsoft.timemeter.ui.model.ActivityCalendar;
import com.simbirsoft.timemeter.ui.util.TimeUtils;
import com.squareup.phrase.Phrase;

import org.slf4j.Logger;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadActivityCalendarJob extends LoadJob implements FilterableJob {

    private static final Logger LOG = LogFactory.getLogger(LoadActivityCalendarJob.class);

    private final DatabaseHelper mDatabaseHelper;
    private TaskLoadFilter mLoadFilter;
    private Date mStartDate;
    private Date mEndDate;

    @Inject
    public LoadActivityCalendarJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
        mLoadFilter = new TaskLoadFilter();
    }

    @Override
    protected LoadJobResult<ActivityCalendar> performLoad() throws Exception {
        final Date startDate = mStartDate;
        final Date endDate = mEndDate;

        Preconditions.checkArgument(startDate != null, "start date is not defined");
        Preconditions.checkArgument(endDate != null, "end date is not defined");

        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();

        final long filterDateMillis = mLoadFilter.getDateMillis();
        final Period filterPeriod = mLoadFilter.getPeriod();
        final Collection<Tag> filterTags = mLoadFilter.getFilterTags();


        long periodStart = 0;
        long periodEnd = 0;
        StringBuilder where = new StringBuilder();
        StringBuilder join = new StringBuilder();
        boolean needJoin = false;

        Phrase queryPhrase = Phrase.from(
                "SELECT {table_tts}.* " +
                        "FROM {table_tts} {join} {where} " +
                        "ORDER BY {table_tts}.{table_tts_column_start_time}")
                .put("table_tts", TaskTimeSpan.TABLE_NAME)
                .put("table_tts_column_start_time", TaskTimeSpan.COLUMN_START_TIME);

        if (!TextUtils.isEmpty(mLoadFilter.getSearchText())) {
            where.append(Phrase.from("{table_task}.{table_task_description} LIKE '%{search_text}%'")
                    .put("table_task", Task.TABLE_NAME)
                    .put("table_task_description", Task.COLUMN_DESCRIPTION)
                    .put("search_text", mLoadFilter.getSearchText())
                    .format());
            needJoin = true;
        }

        if (filterDateMillis > 0) {
            // Select only tasks within given period
            if (!TextUtils.isEmpty(where)) {
                where.append(" AND ");
            }
            periodStart = TimeUtils.getWeekStartMillis(filterDateMillis);
            if (filterPeriod != null) {
                periodEnd = TimeUtils.getWeekEndMillis(Period.getPeriodEnd(filterPeriod, filterDateMillis));
            }
            where.append(QueryUtils.createCalendarPeriodRestrictionStatement(
                    periodStart, periodEnd));
        }

        if (!filterTags.isEmpty()) {
            if (!TextUtils.isEmpty(where)) {
                where.append(" AND ");
            }
            where.append(QueryUtils.createTagsRestrictionStatement(filterTags));
            needJoin = true;
        }

        if (!TextUtils.isEmpty(where)) {
            where.insert(0, "WHERE ");
        }

        if (needJoin) {
            join.append(Phrase.from("INNER JOIN {table_task}" +
                    "ON {table_tts}.{table_tts_column_task_id}={table_task}.{table_task_column_id}")
                    .put("table_task", Task.TABLE_NAME)
                    .put("table_task_column_id", Task.COLUMN_ID)
                    .put("table_tts", TaskTimeSpan.TABLE_NAME)
                    .put("table_tts_column_task_id", TaskTimeSpan.COLUMN_TASK_ID)
                    .format());
        }

        queryPhrase = queryPhrase.put("where", where);
        queryPhrase = queryPhrase.put("join", join);

        final String query = queryPhrase.format().toString();

        if (isCancelled()) {
            LOG.trace("cancelled");
            return LoadJobResult.loadOk();
        }

        ActivityCalendar calendar = new ActivityCalendar();
        Cursor cursor = db.rawQuery(query, new String[0]);
        try {
            List<TaskTimeSpan> spans = cupboard().withCursor(cursor).list(TaskTimeSpan.class);
            if (periodStart == 0) {
                periodStart = (!spans.isEmpty()) ? TimeUtils.getWeekStartMillis(spans.get(0).getStartTimeMillis())
                        : startDate.getTime();
            }
            if (periodEnd == 0) {
                periodEnd = (!spans.isEmpty()) ? TimeUtils.getWeekEndMillis(getMaxEndTime(spans))
                        : endDate.getTime();
            }
            calendar.setPeriodStart(periodStart);
            calendar.setPeriodEnd(periodEnd);
            if (startDate.getTime() < periodStart || endDate.getTime() > periodEnd) {
                if (filterDateMillis > 0) {
                    startDate.setTime(TimeUtils.getWeekStartMillis(filterDateMillis));
                }
                endDate.setTime(TimeUtils.getWeekEndMillis(startDate.getTime()));
            }
            calendar.setStartDate(startDate);
            calendar.setEndDate(endDate);
            calendar.setDailyActivity(spans);
            return new LoadJobResult<>(calendar);
        } catch (Exception e) {
            LOG.trace(e.getStackTrace().toString());
            return LoadJobResult.loadOk();
        }
        finally {
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

    private long getMaxEndTime(List<TaskTimeSpan> list) {
        long time = 0;
        for (TaskTimeSpan item : list) {
            time = Math.max(time, item.getEndTimeMillis());
        }
        return time;
    }
}
