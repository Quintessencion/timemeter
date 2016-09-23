package com.simbirsoft.timeactivity.jobs;

import android.database.Cursor;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.db.model.Tag;
import com.simbirsoft.timeactivity.db.model.Task;
import com.simbirsoft.timeactivity.db.model.TaskTimeSpan;
import com.simbirsoft.timeactivity.injection.Injection;
import com.simbirsoft.timeactivity.ui.model.TaskBundle;
import com.squareup.phrase.Phrase;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadTasksForTimespansJob extends LoadJob {
    private final DatabaseHelper mDatabaseHelper;
    private List<TaskTimeSpan> mSpans;

    @Inject
    public LoadTasksForTimespansJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    public void setSpans(List<TaskTimeSpan> spans) {
        mSpans = spans;
    }

    @Override
    protected void onPreExecute() throws Exception {
        super.onPreExecute();
        Preconditions.checkArgument((mSpans != null && !mSpans.isEmpty()), "spans should be specified");
    }

    @Override
    protected LoadJobResult<List<TaskBundle>> performLoad() throws Exception {
        List<Long> taskIds = ImmutableSet.copyOf(
                Iterables.transform(mSpans, TaskTimeSpan::getTaskId)).asList();
        String ids = Joiner.on(",").join(taskIds);
        final String query = Phrase.from(
                "select * from {table_task} " +
                 "where {table_task_column_id} in ({task_ids})")
                .put("table_task", Task.TABLE_NAME)
                .put("table_task_column_id", Task.COLUMN_ID)
                .put("task_ids", ids)
                .format()
                .toString();
        Cursor cursor = mDatabaseHelper.getWritableDatabase().rawQuery(query, new String[0]);

        try {
            List<Task> tasks = cupboard().withCursor(cursor).list(Task.class);
            Collections.sort(tasks, (item1, item2) ->
                    (taskIds.indexOf(item1.getId()) - taskIds.indexOf(item2.getId())));
            final List<TaskBundle> result = Lists.newArrayListWithCapacity(tasks.size());
            final LoadTaskTagsJob loadJob = Injection.sJobsComponent.loadTaskTagsJob();
            for (Task task : tasks) {
                if (isCancelled()) {
                    return LoadJobResult.loadOk();
                }
                loadJob.setTaskId(task.getId());
                List<Tag> taskTags = ((LoadJobResult<List<Tag>>) forkJob(loadJob).join()).getData();
                TaskBundle taskBundle = TaskBundle.create(task, taskTags);
                Collection taskSpans = Collections2.filter(mSpans, (s) -> (s.getTaskId() == task.getId()));
                        taskBundle.setTaskTimeSpans(Lists.newArrayList(taskSpans));
                result.add(taskBundle);
                loadJob.reset();
            }
            return new LoadJobResult<>(result);

        } finally {
            cursor.close();
        }
    }
}
