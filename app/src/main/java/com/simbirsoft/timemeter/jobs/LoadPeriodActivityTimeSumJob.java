package com.simbirsoft.timemeter.jobs;

import android.database.Cursor;
import android.text.TextUtils;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTag;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.model.Period;
import com.simbirsoft.timemeter.model.TaskLoadFilter;
import com.simbirsoft.timemeter.model.TaskOverallActivity;
import com.simbirsoft.timemeter.util.SqlUtils;
import com.squareup.phrase.Phrase;

import org.slf4j.Logger;

import java.util.Collection;

import javax.inject.Inject;

public class LoadPeriodActivityTimeSumJob extends LoadJob implements FilterableJob {

    private static final Logger LOG = LogFactory.getLogger(LoadPeriodActivityTimeSumJob.class);

    private final DatabaseHelper mDatabaseHelper;
    private TaskLoadFilter mLoadFilter;

    @Inject
    public LoadPeriodActivityTimeSumJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
        mLoadFilter = new TaskLoadFilter();
    }

    @Override
    public void onReset() {
        super.onReset();

        mLoadFilter.clear();
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
    protected LoadJobResult<Integer> performLoad() throws Exception {
        final long filterDateMillis = mLoadFilter.getDateMillis();
        final Period filterPeriod = mLoadFilter.getPeriod();
        final Collection<Tag> filterTags = mLoadFilter.getFilterTags();

        StringBuilder where = null;
        StringBuilder join = null;

        final Phrase queryPhrase = Phrase.from(
                "SELECT " +
                        "SUM({table_tts}.{table_tts_end_time} - {table_tts}.{table_tts_start_time}) AS duration " +
                        "FROM TaskTimeSpan " +
                        "{join} " +
                        "{where} ")
                .put("table_tts", TaskTimeSpan.TABLE_NAME)
                .put("table_tts_start_time", TaskTimeSpan.COLUMN_START_TIME)
                .put("table_tts_end_time", TaskTimeSpan.COLUMN_END_TIME);

        if (filterDateMillis > 0) {
            where = new StringBuilder();
            where.append(SqlUtils.periodToStatement(
                    TaskTimeSpan.TABLE_NAME + "." + TaskTimeSpan.COLUMN_START_TIME,
                    filterDateMillis,
                    filterPeriod));
        }

        if (!filterTags.isEmpty()) {
            if (where == null) {
                where = new StringBuilder();
            } else {
                where.append(" AND ");
            }

            join = new StringBuilder();
            join.append(Phrase.from(
                    "LEFT OUTER JOIN {table_task} " +
                            "ON {table_task}.{table_task_id}={table_tts}.{table_tts_task_id}")
                    .put("table_task", Task.TABLE_NAME)
                    .put("table_task_id", Task.COLUMN_ID)
                    .put("table_tts", TaskTimeSpan.TABLE_NAME)
                    .put("table_tts_task_id", TaskTimeSpan.COLUMN_TASK_ID)
                    .format());

            String tagIds = Joiner.on(",").join(Iterables.transform(filterTags, Tag::getId));

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

        if (!TextUtils.isEmpty(join)) {
            queryPhrase.put("join", join);
        } else {
            queryPhrase.put("join", "");
        }

        if (!TextUtils.isEmpty(where)) {
            where.insert(0, "WHERE ");
            queryPhrase.put("where", where);
        } else {
            queryPhrase.put("where", "");
        }

        final String query = queryPhrase.format().toString();
        final Cursor cursor = mDatabaseHelper.getWritableDatabase().rawQuery(query, new String[0]);
        try {
            if (!cursor.moveToNext()) {
                return new LoadJobResult<>(0);
            }

            Integer result = cursor.getInt(0);
            LOG.trace("computed overall activity time for requested period is '{}", result);

            return new LoadJobResult<>(result);

        } finally {
            cursor.close();
        }
    }
}
