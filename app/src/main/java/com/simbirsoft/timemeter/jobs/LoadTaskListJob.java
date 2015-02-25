package com.simbirsoft.timemeter.jobs;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.be.android.library.worker.exceptions.JobExecutionException;
import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.QueryUtils;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.model.Period;
import com.simbirsoft.timemeter.model.TaskLoadFilter;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.squareup.phrase.Phrase;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadTaskListJob extends LoadJob implements FilterableJob {

    public OrderBy getOrderBy() {
        return mOrderBy;
    }

    public void setOrderBy(OrderBy orderBy) {
        mOrderBy = orderBy;
    }

    public enum OrderBy {
        ALL, CREATION_DATE, RECENT_ACTIVITY
    }

    private final DatabaseHelper mDatabaseHelper;
    private TaskLoadFilter mLoadFilter;
    private boolean mSelectOnlyStartedTasks;
    private OrderBy mOrderBy = OrderBy.ALL;

    @Inject
    public LoadTaskListJob(DatabaseHelper databaseHelper) {
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
    protected LoadJobResult<List<TaskBundle>> performLoad() throws JobExecutionException {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();

        final long filterDateMillis = mLoadFilter.getDateMillis();
        final Period filterPeriod = mLoadFilter.getPeriod();
        final Collection<Tag> filterTags = mLoadFilter.getFilterTags();

        StringBuilder where = new StringBuilder();
        Phrase queryPhrase = Phrase.from(
                "SELECT {table_task}.*, ifnull({table_tts}.{table_tts_column_start_time}, " +
                        "{table_task}.{table_task_column_create_date}) AS begin_time " +
                        "FROM {table_task} " +
                        "{join_type} JOIN {table_tts} " +
                        "ON {table_task}.{table_task_column_id}={table_tts}.{table_tts_column_task_id} " +
                        "{where} " +
                        "GROUP BY {table_task}.{table_task_column_id} " +
                        "ORDER BY {order_by} DESC")
                .put("table_task", Task.TABLE_NAME)
                .put("table_task_column_id", Task.COLUMN_ID)
                .put("table_tts", TaskTimeSpan.TABLE_NAME)
                .put("table_tts_column_task_id", TaskTimeSpan.COLUMN_TASK_ID)
                .put("table_tts_column_start_time", TaskTimeSpan.COLUMN_START_TIME)
                .put("table_task_column_create_date", Task.COLUMN_CREATE_DATE);

        switch (mOrderBy) {
            case ALL:
                queryPhrase.put("order_by", "begin_time");
                break;
            case CREATION_DATE:
                queryPhrase.put("order_by", Task.TABLE_NAME + "." + Task.COLUMN_CREATE_DATE);
                break;

            case RECENT_ACTIVITY:
                queryPhrase.put("order_by", TaskTimeSpan.TABLE_NAME + "." + TaskTimeSpan.COLUMN_START_TIME);
                break;

            default:
                throw new IllegalArgumentException("unexpected order by");
        }

        if (!TextUtils.isEmpty(mLoadFilter.getSearchText())) {
            where.append(Phrase.from("{table_task}.{table_task_description} LIKE '%{search_text}%'")
                    .put("table_task", Task.TABLE_NAME)
                    .put("table_task_description", Task.COLUMN_DESCRIPTION)
                    .put("search_text", mLoadFilter.getSearchText())
                    .format());
        }

        if (filterDateMillis > 0) {
            // Select only tasks within given period
            if (!TextUtils.isEmpty(where)) {
                where.append(" AND ");
            }
            where.append(QueryUtils.createPeriodRestrictionStatement(
                    TaskTimeSpan.TABLE_NAME + "." + TaskTimeSpan.COLUMN_START_TIME,
                    filterDateMillis,
                    filterPeriod));
        }

        if (!filterTags.isEmpty()) {
            if (!TextUtils.isEmpty(where)) {
                where.append(" AND ");
            }

            where.append(QueryUtils.createTagsRestrictionStatement(filterTags));
        }

        if (!TextUtils.isEmpty(where)) {
            where.insert(0, "WHERE ");
        }

        // INNER join in contrast to OUTER will filter out each
        // task not having at least one mapped TaskTimeSpan
        if (mSelectOnlyStartedTasks) {
            queryPhrase.put("join_type", "INNER");
        } else {
            queryPhrase.put("join_type", "LEFT OUTER");
        }

        queryPhrase = queryPhrase.put("where", where);

        final String query = queryPhrase.format().toString();
        Cursor cursor = db.rawQuery(query, new String[0]);
        try {
            final List<Task> tasks = cupboard().withCursor(cursor).list(Task.class);
            final List<TaskBundle> result = Lists.newArrayListWithCapacity(tasks.size());
            final LoadTaskTagsJob loadJob = Injection.sJobsComponent.loadTaskTagsJob();

            for (Task task : tasks) {
                loadJob.setTaskId(task.getId());
                List<Tag> taskTags = ((LoadJobResult<List<Tag>>) forkJob(loadJob).join()).getData();

                result.add(TaskBundle.create(task, taskTags));
                loadJob.reset();
            }

            return new LoadJobResult<>(result);

        } finally {
            cursor.close();
        }
    }

    public boolean isSelectOnlyStartedTasks() {
        return mSelectOnlyStartedTasks;
    }

    public void setSelectOnlyStartedTasks(boolean selectOnlyStartedTasks) {
        mSelectOnlyStartedTasks = selectOnlyStartedTasks;
    }
}
