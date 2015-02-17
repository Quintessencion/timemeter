package com.simbirsoft.timemeter.jobs;

import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.exceptions.JobExecutionException;
import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.model.Period;
import com.simbirsoft.timemeter.model.TaskLoadFilter;
import com.simbirsoft.timemeter.ui.model.DailyActivityDuration;
import com.simbirsoft.timemeter.ui.util.TimeUtils;

import org.slf4j.Logger;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadPeriodActivityTimelineJob extends LoadJob implements FilterableJob {

    private static final Logger LOG = LogFactory.getLogger(LoadPeriodActivityTimelineJob.class);

    private final LoadPeriodActivityTimeSumJob mLoadActivitySumJob;
    private final DatabaseHelper mDatabaseHelper;
    private TaskLoadFilter mLoadFilter;

    @Inject
    public LoadPeriodActivityTimelineJob(LoadPeriodActivityTimeSumJob loadActivitySumJob,
                                         DatabaseHelper databaseHelper) {

        mLoadActivitySumJob = loadActivitySumJob;
        mDatabaseHelper = databaseHelper;
        mLoadFilter = new TaskLoadFilter();
    }

    @Override
    public TaskLoadFilter getTaskLoadFilter() {
        return mLoadFilter;
    }

    @Override
    public void setTaskLoadFilter(TaskLoadFilter filter) {
        mLoadFilter = filter;
    }

    @Override
    protected LoadJobResult<List<DailyActivityDuration>> performLoad() throws Exception {
        long filterDateMillis = mLoadFilter.getDateMillis();
        final Period filterPeriod = mLoadFilter.getPeriod();
        final List<DailyActivityDuration> results = Lists.newArrayList();

        if (filterDateMillis == 0) {
            filterDateMillis = TimeUtils.getDayStartMillis(findActivityBeginDate());

            if (filterDateMillis == 0) {
                return new LoadJobResult<>(results);
            }
        }

        long timelineEndMillis;
        if (filterPeriod == null || filterPeriod == Period.ALL) {
            final Calendar tomorrow = Calendar.getInstance();
            tomorrow.setTimeInMillis(System.currentTimeMillis());
            tomorrow.add(Calendar.DAY_OF_YEAR, 1);
            tomorrow.set(
                    tomorrow.get(Calendar.YEAR),
                    tomorrow.get(Calendar.MONTH),
                    tomorrow.get(Calendar.DAY_OF_MONTH),
                    0,
                    0,
                    0);
            timelineEndMillis = tomorrow.getTimeInMillis();
        } else {
            timelineEndMillis = Period.getPeriodEnd(filterPeriod, filterDateMillis);
        }

        long currentDateMillis = filterDateMillis;
        final long millisPerDay = TimeUnit.DAYS.toMillis(1);
        while (currentDateMillis < timelineEndMillis) {
            int duration = getDurationForDay(currentDateMillis);
            DailyActivityDuration item = new DailyActivityDuration();
            item.date = new Date(currentDateMillis);
            item.duration = duration;
            results.add(item);

            currentDateMillis += millisPerDay;
        }

        return new LoadJobResult<>(results);
    }

    private int getDurationForDay(long dayStartMillis) throws JobExecutionException {
        mLoadActivitySumJob.reset();
        mLoadActivitySumJob.getTaskLoadFilter()
                .dateMillis(dayStartMillis)
                .period(Period.DAY)
                .tags(mLoadFilter.getFilterTags());

        JobEvent result = forkJob(mLoadActivitySumJob).join();

        return ((LoadJobResult<Integer>) result).getData();
    }

    private long findActivityBeginDate() {
        TaskTimeSpan min = cupboard().withDatabase(mDatabaseHelper.getReadableDatabase())
                .query(TaskTimeSpan.class)
                .having("MIN(" + TaskTimeSpan.COLUMN_START_TIME + ")")
                .groupBy(TaskTimeSpan.COLUMN_ID)
                .get();

        if (min == null) {
            return 0;
        }

        return min.getStartTimeMillis();
    }
}