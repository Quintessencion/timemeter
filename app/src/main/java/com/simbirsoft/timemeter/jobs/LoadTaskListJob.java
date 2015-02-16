package com.simbirsoft.timemeter.jobs;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.exceptions.JobExecutionException;
import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.JobResultStatus;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTag;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.model.Period;
import com.simbirsoft.timemeter.model.TaskLoadFilter;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.squareup.phrase.Phrase;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadTaskListJob extends LoadJob implements FilterableJob {

    private final DatabaseHelper mDatabaseHelper;
    private TaskLoadFilter mLoadFilter;

    @Inject
    public LoadTaskListJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
        mLoadFilter = new TaskLoadFilter();

        setGroupId(JobManager.JOB_GROUP_UNIQUE);
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
                        "LEFT OUTER JOIN {table_tts} " +
                        "ON {table_task}.{table_task_column_id}={table_tts}.{table_tts_column_task_id} " +
                        "{where} " +
                        "GROUP BY {table_task}.{table_task_column_id} " +
                        "ORDER BY begin_time DESC")
                .put("table_task", Task.TABLE_NAME)
                .put("table_task_column_id", Task.COLUMN_ID)
                .put("table_tts", TaskTimeSpan.TABLE_NAME)
                .put("table_tts_column_task_id", TaskTimeSpan.COLUMN_TASK_ID)
                .put("table_tts_column_start_time", TaskTimeSpan.COLUMN_START_TIME)
                .put("table_task_column_create_date", Task.COLUMN_CREATE_DATE);

        if (filterDateMillis > 0) {
            // Select only tasks within given period
            long periodEnd = 0;
            if (filterPeriod != null) {
                periodEnd = Period.getPeriodEnd(filterPeriod, filterDateMillis);
            }

            where.append("begin_time >= ").append(filterDateMillis);

            if (periodEnd > 0) {
                where.append(" AND begin_time < ").append(periodEnd);
            }
        }

        if (!filterTags.isEmpty()) {
            // Select only tasks with the queried tags
            String tagIds = Joiner.on(",").join(Iterables.transform(filterTags, Tag::getId));

            if (!TextUtils.isEmpty(where)) {
                where.append(" AND ");
            }

            where.append(Phrase.from(
                    "(SELECT COUNT(*) " +
                            "FROM {table_task_tag} " +
                            "WHERE {table_task_tag}.{table_task_tag_column_task_id}={table_task}.{table_task_column_task_id} " +
                            "AND {table_task_tag}.{table_task_tag_column_tag_id} IN ({tag_ids}) " +
                            "GROUP BY {table_task_tag}.{table_task_tag_column_task_id})={tag_count}")
                    .put("table_task", Task.TABLE_NAME)
                    .put("table_task_tag", TaskTag.TABLE_NAME)
                    .put("table_task_column_task_id", Task.COLUMN_ID)
                    .put("table_task_tag_column_task_id", TaskTag.COLUMN_TASK_ID)
                    .put("table_task_tag_column_tag_id", TaskTag.COLUMN_TAG_ID)
                    .put("tag_ids", tagIds)
                    .put("tag_count", filterTags.size())
                    .format());
        }

        if (!TextUtils.isEmpty(where)) {
            where.insert(0, "WHERE ");
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
}
