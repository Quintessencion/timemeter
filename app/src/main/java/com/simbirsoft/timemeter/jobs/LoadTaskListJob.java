package com.simbirsoft.timemeter.jobs;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.exceptions.JobExecutionException;
import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.JobResultStatus;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTag;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.squareup.phrase.Phrase;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadTaskListJob extends LoadJob {

    public static class LoadFilter {
        private final Set<Tag> mFilterTags;

        LoadFilter() {
            mFilterTags = Sets.newHashSet();
        }

        public LoadFilter tags(Collection<Tag> tags) {
            mFilterTags.addAll(tags);

            return this;
        }
    }

    private final DatabaseHelper mDatabaseHelper;
    private final LoadFilter mLoadFilter;

    @Inject
    public LoadTaskListJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
        mLoadFilter = new LoadFilter();

        setGroupId(JobManager.JOB_GROUP_UNIQUE);
    }

    public LoadFilter getLoadFilter() {
        return mLoadFilter;
    }

    @Override
    protected LoadJobResult<?> performLoad() throws JobExecutionException {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();

        final String query;
        if (mLoadFilter.mFilterTags.isEmpty()) {
            query = Phrase.from("SELECT {table_task}.* " +
                    "FROM {table_task} " +
                    "ORDER BY {table_task_column_create_date}")
                    .put("table_task", Task.TABLE_NAME)
                    .put("table_task_column_create_date", Task.COLUMN_CREATE_DATE)
                    .format()
                    .toString();
        } else {
            String tagIds = Joiner.on(",").join(Iterables.transform(mLoadFilter.mFilterTags, Tag::getId));

            query = Phrase.from("SELECT {table_task}.* " +
                    "FROM {table_task} " +
                    "JOIN {table_task_tag} " +
                    "ON {table_task}.{table_task_column_id}={table_task_tag_column_task_id} " +
                    "WHERE {table_task_tag_column_tag_id} IN ({tag_ids}) " +
                    "GROUP BY {table_task}.{table_task_column_id} " +
                    "HAVING COUNT({table_task_tag_column_task_id})={tag_count} " +
                    "ORDER BY {table_task_column_create_date}")
                    .put("table_task", Task.TABLE_NAME)
                    .put("table_task_tag", TaskTag.TABLE_NAME)
                    .put("table_task_column_id", Task.COLUMN_ID)
                    .put("table_task_tag_column_task_id", TaskTag.COLUMN_TASK_ID)
                    .put("table_task_tag_column_tag_id", TaskTag.COLUMN_TAG_ID)
                    .put("tag_ids", tagIds)
                    .put("table_task_column_create_date", Task.COLUMN_CREATE_DATE)
                    .put("tag_count", mLoadFilter.mFilterTags.size())
                    .format()
                    .toString();
        }

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

            return new LoadJobResult<>(JobResultStatus.OK, result);

        } finally {
            cursor.close();
        }
    }
}
