package com.simbirsoft.timeactivity.jobs;

import android.database.Cursor;
import android.text.TextUtils;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Lists;
import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.db.QueryUtils;
import com.simbirsoft.timeactivity.db.model.Tag;
import com.simbirsoft.timeactivity.db.model.Task;
import com.simbirsoft.timeactivity.db.model.TaskTimeSpan;
import com.simbirsoft.timeactivity.log.LogFactory;
import com.simbirsoft.timeactivity.model.Period;
import com.simbirsoft.timeactivity.model.TaskLoadFilter;
import com.squareup.phrase.Phrase;

import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

/**
 * Returns list of array with {task_id, duration}
 * Suitable to display activity chart splitted by tasks (used in stacked bar chart)
 */
public class LoadPeriodActivitySplitTimeSumJob extends LoadJob implements FilterableJob {

    private static final Logger LOG = LogFactory.getLogger(LoadPeriodActivitySplitTimeSumJob.class);

    private static String sTaskJoin;

    private final DatabaseHelper mDatabaseHelper;
    private TaskLoadFilter mLoadFilter;

    @Inject
    public LoadPeriodActivitySplitTimeSumJob(DatabaseHelper databaseHelper) {
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
    protected void onPreExecute() throws Exception {
        if (sTaskJoin == null) {
            sTaskJoin = Phrase.from(
                    "JOIN {table_task} " +
                            "ON {table_task}.{table_task_id}={table_tts}.{table_tts_task_id}")
                    .put("table_task", Task.TABLE_NAME)
                    .put("table_task_id", Task.COLUMN_ID)
                    .put("table_tts", TaskTimeSpan.TABLE_NAME)
                    .put("table_tts_task_id", TaskTimeSpan.COLUMN_TASK_ID)
                    .format()
                    .toString();
        }
    }

    @Override
    protected LoadJobResult<List<long[]>> performLoad() throws Exception {
        final List<long[]> results = Lists.newArrayList();
        final long filterDateMillis = mLoadFilter.getStartDateMillis();
        final Period filterPeriod = mLoadFilter.getPeriod();
        final Collection<Tag> filterTags = mLoadFilter.getFilterTags();

        StringBuilder where = new StringBuilder();
        String join = "";

        final Phrase queryPhrase = Phrase.from(
                "SELECT {table_tts}.{table_tts_task_id}, " +
                        "SUM({table_tts}.{table_tts_end_time} - {table_tts}.{table_tts_start_time}) AS duration " +
                        "FROM TaskTimeSpan " +
                        "{join} " +
                        "{where} " +
                        "GROUP BY {table_tts}.{table_tts_task_id} ")
                .put("table_tts", TaskTimeSpan.TABLE_NAME)
                .put("table_tts_task_id", TaskTimeSpan.COLUMN_TASK_ID)
                .put("table_tts_start_time", TaskTimeSpan.COLUMN_START_TIME)
                .put("table_tts_end_time", TaskTimeSpan.COLUMN_END_TIME);

        if (!TextUtils.isEmpty(mLoadFilter.getSearchText())) {
            join = sTaskJoin;

            where.append(Phrase.from("{table_task}.{table_task_description} LIKE '%{search_text}%'")
                    .put("table_task", Task.TABLE_NAME)
                    .put("table_task_description", Task.COLUMN_DESCRIPTION)
                    .put("search_text", mLoadFilter.getSearchText())
                    .format());
        }

        if (filterDateMillis > 0) {
            if (!TextUtils.isEmpty(where)) {
                where.append(" AND ");
            }
            where.append(QueryUtils.createPeriodRestrictionStatement(
                    TaskTimeSpan.TABLE_NAME + "." + TaskTimeSpan.COLUMN_START_TIME,
                    filterDateMillis,
                    filterPeriod));
        }

        if (!filterTags.isEmpty()) {
            join = sTaskJoin;

            if (!TextUtils.isEmpty(where)) {
                where.append(" AND ");
            }
            where.append(QueryUtils.createTagsRestrictionStatement(filterTags));
        }

        queryPhrase.put("join", join);

        if (!TextUtils.isEmpty(where)) {
            where.insert(0, "WHERE ");
            queryPhrase.put("where", where);
        } else {
            queryPhrase.put("where", "");
        }

        final String query = queryPhrase.format().toString();
        final Cursor cursor = mDatabaseHelper.getWritableDatabase().rawQuery(query, new String[0]);
        try {

            while (cursor.moveToNext()) {
                results.add(new long[]{cursor.getLong(0), cursor.getLong(1)});
            }

            return new LoadJobResult<>(results);

        } finally {
            cursor.close();
        }
    }
}
