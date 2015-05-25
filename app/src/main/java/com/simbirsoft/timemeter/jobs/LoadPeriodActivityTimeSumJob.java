package com.simbirsoft.timemeter.jobs;

import android.database.Cursor;
import android.text.TextUtils;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.QueryUtils;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.model.Period;
import com.simbirsoft.timemeter.model.TaskLoadFilter;
import com.squareup.phrase.Phrase;

import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

public class LoadPeriodActivityTimeSumJob extends LoadJob implements FilterableJob {

    private static final Logger LOG = LogFactory.getLogger(LoadPeriodActivityTimeSumJob.class);

    private static String sTaskJoin;

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
    protected LoadJobResult<Integer> performLoad() throws Exception {
        final long filterDateMillis = mLoadFilter.getStartDateMillis();
        final Period filterPeriod = mLoadFilter.getPeriod();
        final Collection<Tag> filterTags = mLoadFilter.getFilterTags();
        final List<Long> taskIds = mLoadFilter.getTaskIds();

        StringBuilder where = null;
        String join = "";

        final Phrase queryPhrase = Phrase.from(
                "SELECT " +
                        "SUM({table_tts}.{table_tts_end_time} - {table_tts}.{table_tts_start_time}) AS duration " +
                        "FROM TaskTimeSpan " +
                        "{join} " +
                        "{where} ")
                .put("table_tts", TaskTimeSpan.TABLE_NAME)
                .put("table_tts_start_time", TaskTimeSpan.COLUMN_START_TIME)
                .put("table_tts_end_time", TaskTimeSpan.COLUMN_END_TIME);

        if (!TextUtils.isEmpty(mLoadFilter.getSearchText())) {
            join = sTaskJoin;

            where = new StringBuilder(
                    Phrase.from("{table_task}.{table_task_description} LIKE '%{search_text}%'")
                            .put("table_task", Task.TABLE_NAME)
                            .put("table_task_description", Task.COLUMN_DESCRIPTION)
                            .put("search_text", mLoadFilter.getSearchText())
                            .format());
        }

        if (filterDateMillis > 0) {
            if (where == null) {
                where = new StringBuilder();
            } else {
                where.append(" AND ");
            }
            where.append(QueryUtils.createPeriodRestrictionStatement(
                    TaskTimeSpan.TABLE_NAME + "." + TaskTimeSpan.COLUMN_START_TIME,
                    filterDateMillis,
                    filterPeriod));
        }

        if (taskIds != null && taskIds.size() > 0) {
            join = sTaskJoin + QueryUtils.createTaskIdsRestrictionStatement(taskIds);
        }

        if (!filterTags.isEmpty()) {
            join = sTaskJoin;

            if (where == null) {
                where = new StringBuilder();
            } else {
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
