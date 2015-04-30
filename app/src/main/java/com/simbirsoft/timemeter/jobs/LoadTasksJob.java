package com.simbirsoft.timemeter.jobs;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.JobResultStatus;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.QueryUtils;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.model.TaskLoadFilter;
import com.simbirsoft.timemeter.ui.model.ActivityCalendar;
import com.simbirsoft.timemeter.ui.model.CalendarData;
import com.simbirsoft.timemeter.ui.model.CalendarPeriod;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.util.TimeUtils;
import com.squareup.phrase.Phrase;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadTasksJob extends LoadJob {
    private final DatabaseHelper mDatabaseHelper;
    private List<TaskTimeSpan> mSpans;

    @Inject
    public LoadTasksJob(DatabaseHelper databaseHelper) {
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
        List<Long> taskIds = Lists.newArrayList();
        StringBuilder idsSet = new StringBuilder();
        for (TaskTimeSpan span : mSpans) {
            if (!taskIds.contains(span.getTaskId())) {
                if (!taskIds.isEmpty()) {
                    idsSet.append(',');
                }
                idsSet.append(span.getTaskId());
                taskIds.add(span.getTaskId());
            }
        }
        final String query = Phrase.from(
                "select * from {table_task} " +
                 "where {table_task_column_id} in ({task_ids})")
                .put("table_task", Task.TABLE_NAME)
                .put("table_task_column_id", Task.COLUMN_ID)
                .put("task_ids", idsSet.toString())
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
                result.add(TaskBundle.create(task, taskTags));
                loadJob.reset();
            }
            return new LoadJobResult<>(result);

        } finally {
            cursor.close();
        }
    }
}
