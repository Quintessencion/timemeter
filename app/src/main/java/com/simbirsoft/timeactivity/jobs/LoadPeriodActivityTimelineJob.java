package com.simbirsoft.timeactivity.jobs;

import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.exceptions.JobExecutionException;
import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Lists;
import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.db.QueryHelper;
import com.simbirsoft.timeactivity.log.LogFactory;
import com.simbirsoft.timeactivity.model.Period;
import com.simbirsoft.timeactivity.model.TaskLoadFilter;
import com.simbirsoft.timeactivity.ui.model.DailyActivityDuration;
import com.simbirsoft.timeactivity.ui.util.TimeUtils;

import org.slf4j.Logger;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

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
        long[] dateBounds = new long[2];
        mLoadFilter.getDateBounds(dateBounds);
        long filterStartDateMillis = dateBounds[0];
        long filterEndDateMillis = dateBounds[1];
        Period filterPeriod = mLoadFilter.getPeriod();
        final List<DailyActivityDuration> results = Lists.newArrayList();

        if (filterStartDateMillis == 0) {
            long firstActivityBeginDate = QueryHelper.findFirstActivityBeginDate(mDatabaseHelper.getReadableDatabase());
            filterStartDateMillis = (firstActivityBeginDate == 0) ? 0 : TimeUtils.getDayStartMillis(firstActivityBeginDate);

            if (filterStartDateMillis == 0) {
                LOG.debug("unable to load activity timeline: no activity found");

                return new LoadJobResult<>(results);
            }
        }

        long timelineEndMillis;
        if (filterPeriod == null || filterPeriod == Period.ALL) {
            timelineEndMillis = TimeUtils.tomorrowStart();
        } else {
            timelineEndMillis = filterEndDateMillis;
        }
        timelineEndMillis = (timelineEndMillis / 1000) * 1000;

        final Calendar currentDate = Calendar.getInstance();
        currentDate.setTimeInMillis(filterStartDateMillis);

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
                .startDateMillis(dayStartMillis)
                .period(Period.DAY)
                .tags(mLoadFilter.getFilterTags())
                .searchText(mLoadFilter.getSearchText())
                .taskIds(mLoadFilter.getTaskIds());

        JobEvent result = forkJob(mLoadActivitySumJob).join();

        return ((LoadJobResult<Integer>) result).getData();
    }
}
