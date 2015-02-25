package com.simbirsoft.timemeter.jobs;

import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.exceptions.JobExecutionException;
import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.QueryHelper;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.model.Period;
import com.simbirsoft.timemeter.model.TaskLoadFilter;
import com.simbirsoft.timemeter.model.TaskOverallActivity;
import com.simbirsoft.timemeter.ui.model.DailyTaskActivityDuration;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.util.TimeUtils;

import org.slf4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class LoadPeriodSplitActivityTimelineJob extends LoadJob implements FilterableJob {

    private static final Logger LOG = LogFactory.getLogger(LoadPeriodSplitActivityTimelineJob.class);

    private final LoadTaskListJob mLoadTaskListJob;
    private final LoadPeriodActivitySplitTimeSumJob mLoadActivitySplitSumJob;
    private final DatabaseHelper mDatabaseHelper;
    private TaskLoadFilter mLoadFilter;

    @Inject
    public LoadPeriodSplitActivityTimelineJob(LoadTaskListJob loadTaskListJob,
              LoadPeriodActivitySplitTimeSumJob loadActivitySplitSumJob,
              DatabaseHelper databaseHelper) {

        mLoadTaskListJob = loadTaskListJob;

        mLoadActivitySplitSumJob = loadActivitySplitSumJob;
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
    protected LoadJobResult<List<DailyTaskActivityDuration>> performLoad() throws Exception {
        long filterDateMillis = mLoadFilter.getDateMillis();
        final Period filterPeriod = mLoadFilter.getPeriod();
        final List<DailyTaskActivityDuration> results = Lists.newArrayList();
        final List<TaskBundle> tasks = loadTasks();
        final int taskCount = tasks.size();

        if (filterDateMillis == 0) {
            filterDateMillis = TimeUtils.getDayStartMillis(
                    QueryHelper.findFirstActivityBeginDate(mDatabaseHelper.getReadableDatabase()));

            if (filterDateMillis == 0) {
                LOG.debug("unable to load split activity timeline: no activity found");

                return new LoadJobResult<>(results);
            }
        }

        long timelineEndMillis;
        if (filterPeriod == null || filterPeriod == Period.ALL) {
            timelineEndMillis = TimeUtils.tomorrowStart();
        } else {
            timelineEndMillis = Period.getPeriodEnd(filterPeriod, filterDateMillis);
        }

        long currentDateMillis = filterDateMillis;
        final long millisPerDay = TimeUnit.DAYS.toMillis(1);
        while (currentDateMillis < timelineEndMillis) {
            final Date currentDate = new Date(currentDateMillis);

            int i = 0;
            TaskOverallActivity[] activity = new TaskOverallActivity[taskCount];
            for (TaskBundle taskBundle : tasks) {
                activity[i] = new TaskOverallActivity(taskBundle.getTask());
                i++;
            }
            fillDurationForDate(currentDateMillis, activity);

            DailyTaskActivityDuration item = new DailyTaskActivityDuration();
            item.date = currentDate;
            item.tasks = activity;

            results.add(item);

            currentDateMillis += millisPerDay;
        }

        return new LoadJobResult<>(results);
    }

    private List<TaskBundle> loadTasks() throws JobExecutionException {
        mLoadTaskListJob.setTaskLoadFilter(mLoadFilter);
        mLoadTaskListJob.setSelectOnlyStartedTasks(true);
        mLoadTaskListJob.setOrderBy(LoadTaskListJob.OrderBy.CREATION_DATE);

        return ((LoadJobResult<List<TaskBundle>>) forkJob(mLoadTaskListJob).join()).getData();
    }

    private void fillDurationForDate(long dayStartMillis, TaskOverallActivity[] activity)
            throws JobExecutionException {

        mLoadActivitySplitSumJob.reset();
        mLoadActivitySplitSumJob.getTaskLoadFilter()
                .dateMillis(dayStartMillis)
                .period(Period.DAY)
                .tags(mLoadFilter.getFilterTags())
                .searchText(mLoadFilter.getSearchText());

        JobEvent result = forkJob(mLoadActivitySplitSumJob).join();

        final List<long[]> tasksDuration = ((LoadJobResult<List<long[]>>) result).getData();

        for (TaskOverallActivity entry : activity) {
            int index = Iterables.indexOf(tasksDuration, (input) -> input[0] == entry.getId());
            entry.setDuration(index > -1 ? tasksDuration.get(index)[1] : 0L);
        }
    }
}

