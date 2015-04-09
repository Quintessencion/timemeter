package com.simbirsoft.timemeter.db;

import android.support.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTag;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.model.Period;
import com.simbirsoft.timemeter.ui.util.TimeUtils;
import com.squareup.phrase.Phrase;

import java.util.Collection;

public final class QueryUtils {

    /**
     * Create where-appendable statement to restrict Task lookup with specified tags only.
     * @param tags
     * @return
     */
    public static CharSequence createTagsRestrictionStatement(Collection<Tag> tags) {
        final String tagIds = Joiner.on(",").join(Iterables.transform(tags, Tag::getId));

        return Phrase.from(
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
                .put("tag_count", tags.size())
                .format();
    }

    /**
     * @param dateMillis period beginning
     * @param period optional period
     * @return SQL period selection statement or empty string
     * if <code>dateMillis</code> is <code>0</code>.
     */
    public static String createPeriodRestrictionStatement(
            String beginTimeColumnName, long dateMillis, @Nullable Period period) {

        String where = "";

        if (dateMillis == 0) {
            return where;
        }

        long periodEnd = 0;
        if (period != null) {
            periodEnd = Period.getPeriodEnd(period, dateMillis);
        }

        where += beginTimeColumnName + " >= " + String.valueOf(dateMillis);

        if (periodEnd > 0) {
            where += " AND " + beginTimeColumnName + " < " + String.valueOf(periodEnd);
        }

        return where;
    }
}
