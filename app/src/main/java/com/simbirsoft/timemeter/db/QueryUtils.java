package com.simbirsoft.timemeter.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTag;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.model.Period;
import com.squareup.phrase.Phrase;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

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

    public static CharSequence createTagsRestrictionStatementForTimeSpan(Collection<Tag> tags) {
        final String tagIds = Joiner.on(",").join(Iterables.transform(tags, Tag::getId));

        return Phrase.from(
                "(SELECT COUNT(*) " +
                        "FROM {table_task_tag} " +
                        "WHERE {table_task_tag}.{table_task_tag_column_task_id}={table_tts}.{table_tts_column_task_id} " +
                        "AND {table_task_tag}.{table_task_tag_column_tag_id} IN ({tag_ids}) " +
                        "GROUP BY {table_task_tag}.{table_task_tag_column_task_id})={tag_count}")
                .put("table_tts", TaskTimeSpan.TABLE_NAME)
                .put("table_task_tag", TaskTag.TABLE_NAME)
                .put("table_tts_column_task_id", TaskTimeSpan.COLUMN_TASK_ID)
                .put("table_task_tag_column_task_id", TaskTag.COLUMN_TASK_ID)
                .put("table_task_tag_column_tag_id", TaskTag.COLUMN_TAG_ID)
                .put("tag_ids", tagIds)
                .put("tag_count", tags.size())
                .format();
    }

    public static String createTaskIdsRestrictionStatement(Collection<Long> taskIds) {
        final String taskIdsCommaSeparated = Joiner.on(",").join(Iterables.transform(taskIds, taskId -> taskId));

        StringBuilder taskIdSBuilder = new StringBuilder();
        taskIdSBuilder.append(" AND " + Task.TABLE_NAME + "." + Task.COLUMN_ID + " IN (");
        taskIdSBuilder.append(taskIdsCommaSeparated);
        taskIdSBuilder.append(")");

        return taskIdSBuilder.toString();
    }

    public static List<Tag> getTagsForTask(SQLiteDatabase db, long taskId) {
        List<Tag> tags = Collections.emptyList();

        final String query = Phrase.from(
                "select * " +
                        "from {table_tag} " +
                        "where {table_tag}.{table_tag_column_tag_id} in " +
                        "(select {table_tasktag}.{table_tasktag_column_tag_id} " +
                        "from {table_tasktag} " +
                        "where {table_tasktag}.{table_tasktag_column_task_id}=?)")
                .put("table_tag", Tag.TABLE_NAME)
                .put("table_tag_column_tag_id", Tag.COLUMN_ID)
                .put("table_tasktag", TaskTag.TABLE_NAME)
                .put("table_tasktag_column_tag_id", TaskTag.COLUMN_TAG_ID)
                .put("table_tasktag_column_task_id", TaskTag.COLUMN_TASK_ID)
                .format()
                .toString();

        String[] args = new String[]{taskId + ""};
        Cursor c = db.rawQuery(query, args);

        try {
            tags = cupboard().withCursor(c).list(Tag.class);
        } finally {
            c.close();
        }

        return tags;
    }
}