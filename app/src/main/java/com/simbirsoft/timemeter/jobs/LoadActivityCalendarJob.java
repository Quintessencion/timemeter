package com.simbirsoft.timemeter.jobs;

import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.model.Period;
import com.simbirsoft.timemeter.model.TaskLoadFilter;
import com.simbirsoft.timemeter.ui.model.ActivityCalendar;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadActivityCalendarJob extends LoadJob implements FilterableJob {

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
        // TODO: apply task load filter to sql as appropriate
        // (see LoadTaskListJob, LoadOverallTaskActivityTimeJob, LoadPeriodActivityTimeSumJob)

        ActivityCalendar calendar = new ActivityCalendar();
        List<TaskTimeSpan> spans = cupboard().withDatabase(db)
                .query(TaskTimeSpan.class)
                .list();
        calendar.setStartDate(startDate);
        calendar.setEndDate(endDate);
        calendar.setDailyActivity(spans);

        return new LoadJobResult<>(calendar);
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
}
