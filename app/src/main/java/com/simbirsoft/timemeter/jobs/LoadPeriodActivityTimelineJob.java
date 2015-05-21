package com.simbirsoft.timemeter.jobs;

import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.exceptions.JobExecutionException;
import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.QueryHelper;
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

/**
 * Used in line chart.
 */
public class LoadPeriodActivityTimelineJob extends LoadJob implements FilterableJob {

    private static final Logger LOG = LogFactory.getLogger(LoadPeriodActivityTimelineJob.class);

    private final LoadPeriodActivityTimeSumJob mLoadActivitySumJob;
    private final DatabaseHelper mDatabaseHelper;
    private TaskLoadFilter mLoadFilter;
    private long mOverallActivityDuration;

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
    protected void onPreExecute() throws Exception {
        mOverallActivityDuration = 0;
    }

    @Override
    protected LoadJobResult<List<DailyActivityDuration>> performLoad() throws Exception {
        long filterDateMillis = mLoadFilter.getDateMillis();
        final Period filterPeriod = mLoadFilter.getPeriod();
        final List<DailyActivityDuration> results = Lists.newArrayList();

        if (filterDateMillis == 0) {
            filterDateMillis = TimeUtils.getDayStartMillis(
                    QueryHelper.findFirstActivityBeginDate(mDatabaseHelper.getReadableDatabase()));

            if (filterDateMillis == 0) {
                LOG.debug("unable to load activity timeline: no activity found");

                return new LoadJobResult<>(results);
            }
        }

        long timelineEndMillis;
        if (filterPeriod == null || filterPeriod == Period.ALL) {
            timelineEndMillis = TimeUtils.tomorrowStart();
        } else {
            timelineEndMillis = Period.getPeriodEnd(filterPeriod, filterDateMillis);
        }
        timelineEndMillis = (timelineEndMillis / 1000) * 1000;

        final Calendar currentDate = Calendar.getInstance();
        currentDate.setTimeInMillis(filterDateMillis);

        while (true) {
            long currentTime = (currentDate.getTimeInMillis() / 1000) * 1000;
            if (currentTime >= timelineEndMillis) {
                break;
            }

            int duration = getDurationForDay(currentTime);
            DailyActivityDuration item = new DailyActivityDuration();
            item.date = new Date(currentTime);
            item.duration = duration;
            results.add(item);

            mOverallActivityDuration += duration;

            currentDate.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (mOverallActivityDuration == 0) {
            // None results
            results.clear();
        }

        return new LoadJobResult<>(results);
    }

    private int getDurationForDay(long dayStartMillis) throws JobExecutionException {
        mLoadActivitySumJob.reset();
        mLoadActivitySumJob.getTaskLoadFilter()
                .dateMillis(dayStartMillis)
                .period(Period.DAY)
                .tags(mLoadFilter.getFilterTags())
                .searchText(mLoadFilter.getSearchText())
                .taskIds(mLoadFilter.getTaskIds());

        JobEvent result = forkJob(mLoadActivitySumJob).join();

        return ((LoadJobResult<Integer>) result).getData();
    }
}
