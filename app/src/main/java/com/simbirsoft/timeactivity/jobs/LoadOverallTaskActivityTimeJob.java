package com.simbirsoft.timeactivity.jobs;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Lists;
import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.db.QueryUtils;
import com.simbirsoft.timeactivity.db.TaskOverallTimeEntityConverter;
import com.simbirsoft.timeactivity.db.model.Tag;
import com.simbirsoft.timeactivity.db.model.Task;
import com.simbirsoft.timeactivity.db.model.TaskTimeSpan;
import com.simbirsoft.timeactivity.log.LogFactory;
import com.simbirsoft.timeactivity.model.Period;
import com.simbirsoft.timeactivity.model.TaskLoadFilter;
import com.simbirsoft.timeactivity.model.TaskOverallActivity;
import com.squareup.phrase.Phrase;

import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.convert.EntityConverter;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Loaded data used in pie chart.
 */
public class LoadOverallTaskActivityTimeJob extends LoadJob implements FilterableJob {

    private static final Logger LOG = LogFactory.getLogger(LoadOverallTaskActivityTimeJob.class);

    private static final float MAX_ACCUMULATED_AMOUNT_PERCENTAGE = 0.3f;
    private static final float ACCUMULATE_THRESHOLD_PERCENTAGE = 0.02f;

    private final Context mContext;
    private final DatabaseHelper mDatabaseHelper;
    private TaskLoadFilter mLoadFilter;

    @Inject
    public LoadOverallTaskActivityTimeJob(Context context, DatabaseHelper databaseHelper) {
        mContext = context;
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
    protected LoadJobResult<List<TaskOverallActivity>> performLoad() throws Exception {
        final long filterDateMillis = mLoadFilter.getStartDateMillis();
        final Period filterPeriod = mLoadFilter.getPeriod();
        final Collection<Tag> filterTags = mLoadFilter.getFilterTags();

        StringBuilder where = new StringBuilder();

        final Phrase queryPhrase = Phrase.from(
                "SELECT " +
                    "SUM({table_tts}.{table_tts_end_time} - {table_tts}.{table_tts_start_time}) AS {duration}, " +
                    "{table_task}.* " +
                "FROM Task " +
                "JOIN {table_tts} ON {table_tts}.{table_tts_task_id}={table_task}.{table_task_id} " +
                "{where} " +
                "GROUP BY {table_task}.{table_task_id} " +
                "ORDER BY {duration}")
                .put("table_task", Task.TABLE_NAME)
                .put("table_tts", TaskTimeSpan.TABLE_NAME)
                .put("table_tts_start_time", TaskTimeSpan.COLUMN_START_TIME)
                .put("table_tts_end_time", TaskTimeSpan.COLUMN_END_TIME)
                .put("table_tts_task_id", TaskTimeSpan.COLUMN_TASK_ID)
                .put("table_task_id", Task.COLUMN_ID)
                .put("duration", TaskOverallActivity.COLUMN_OVERALL_DURATION);

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
        queryPhrase.put("where", where);

        final Cupboard cupboard = cupboard();
        final EntityConverter<TaskOverallActivity> converter =
                new TaskOverallTimeEntityConverter(cupboard, TaskOverallActivity.class);
        final String query = queryPhrase.format().toString();
        final Cursor cursor = mDatabaseHelper.getWritableDatabase().rawQuery(query, new String[0]);
        final List<TaskOverallActivity> results = Lists.newArrayListWithCapacity(cursor.getCount());
        try {
            while (cursor.moveToNext()) {
                results.add(converter.fromCursor(cursor));
            }

            LOG.trace("selected {} tasks for overall activity", results.size());

            final List<TaskOverallActivity> resultsAccumulated = accumulateResults(results);
            Collections.reverse(resultsAccumulated);

            return new LoadJobResult<>(resultsAccumulated);

        } finally {
            cursor.close();
        }
    }

    private List<TaskOverallActivity> accumulateResults(List<TaskOverallActivity> input) {
        int durationSum = 0;
        for (TaskOverallActivity item : input) {
            durationSum += item.getDuration();
        }
        final LinkedList<TaskOverallActivity> result = Lists.newLinkedList();
        final LinkedList<TaskOverallActivity> filteredResults = Lists.newLinkedList();
        double accumulatedAmount = 0;
        long accumulatedDuration = 0;

        for (TaskOverallActivity item : input) {
            float durationRatio = item.getDuration() / (float) durationSum;
            item.setDurationRatio(durationRatio);
            if (accumulatedAmount < MAX_ACCUMULATED_AMOUNT_PERCENTAGE
                    && durationRatio < ACCUMULATE_THRESHOLD_PERCENTAGE) {

                accumulatedAmount += item.getDurationRatio();
                accumulatedDuration += item.getDuration();
                filteredResults.add(item);

            } else {
                result.add(item);
            }
        }

        // Do not include single item to accumulated results
        if (filteredResults.size() <= 2) {
            result.addAll(0, filteredResults);
        } else {
            // Pop last accumulated item
            TaskOverallActivity extraAccumulatedItem = filteredResults.removeLast();
            result.addFirst(extraAccumulatedItem);
            accumulatedDuration -= extraAccumulatedItem.getDuration();
            accumulatedAmount -= extraAccumulatedItem.getDurationRatio();

            // Resolve the rest of accumulation
            TaskOverallActivity accumulatedItem = new TaskOverallActivity();
            accumulatedItem.setDuration(accumulatedDuration);
            accumulatedItem.setDurationRatio((float) accumulatedAmount);
            accumulatedItem.setDescription(mContext.getString(
                    R.string.caption_accumulated_tasks_duration));
            result.addFirst(accumulatedItem);
        }

        return result;
    }
}
