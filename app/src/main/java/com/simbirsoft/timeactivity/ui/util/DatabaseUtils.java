package com.simbirsoft.timeactivity.ui.util;

import android.content.Context;

import com.google.common.io.Closeables;
import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.db.model.DemoTask;
import com.simbirsoft.timeactivity.db.model.Tag;
import com.simbirsoft.timeactivity.db.model.Task;
import com.simbirsoft.timeactivity.db.model.TaskTag;
import com.simbirsoft.timeactivity.db.model.TaskTimeSpan;
import com.simbirsoft.timeactivity.log.LogFactory;
import com.simbirsoft.timeactivity.persist.XmlTag;
import com.simbirsoft.timeactivity.persist.XmlTagRef;
import com.simbirsoft.timeactivity.persist.XmlTask;
import com.simbirsoft.timeactivity.persist.XmlTaskList;
import com.simbirsoft.timeactivity.persist.XmlTaskListReader;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import nl.qbusict.cupboard.DatabaseCompartment;

public final class DatabaseUtils {

    private static final Logger LOG = LogFactory.getLogger(DatabaseHelper.class);

    public static void fillTestData(Context context, DatabaseCompartment cupboard) {
        InputStream in = null;
        try {
            in = context.getAssets().open("testdata/tasklist-ru.xml");
            XmlTaskList taskList = XmlTaskListReader.readXml(in);
            LOG.trace("task list read successfully");

            for (XmlTag xmlTag : taskList.getTagList()) {
                Tag tag = xmlTag.getTag();
                cupboard.put(tag);
            }

            for (XmlTask xmlTask : taskList.getTaskList()) {
                Task task = xmlTask.getTask();
                cupboard.put(task);
                cupboard.put(new DemoTask(task));
                xmlTask.setId(task.getId());
                List<TaskTimeSpan> spans = actualizeTaskActivities(xmlTask.getTaskActivity());
                cupboard.put(spans);

                for (XmlTagRef tagRef : xmlTask.getTagList()) {
                    TaskTag taskTag = new TaskTag();
                    taskTag.setTaskId(task.getId());
                    taskTag.setTagId(tagRef.getTagId());
                    cupboard.put(taskTag);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            Closeables.closeQuietly(in);
        }
    }

    private static Calendar getDay(long timeInMillis, int prevDay) {
        Calendar spanCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        spanCalendar.setTimeInMillis(timeInMillis);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                spanCalendar.get(Calendar.HOUR_OF_DAY), spanCalendar.get(Calendar.MINUTE), spanCalendar.get(Calendar.SECOND));
        calendar.add(Calendar.DAY_OF_YEAR, prevDay);

        return calendar;
    }

    public static List<TaskTimeSpan> actualizeTaskActivities(List<TaskTimeSpan> spans) {
        long startTimeMillis = 0;
        long endTimeMillis = 0;

        int index = 1;

        for (TaskTimeSpan span : spans) {
            int day = (-spans.size()) + index;
            index++;

            Calendar calendar = getDay(span.getStartTimeMillis(), day);
            span.setStartTimeMillis(calendar.getTimeInMillis());

            calendar = getDay(span.getEndTimeMillis(), day);
            span.setEndTimeMillis(calendar.getTimeInMillis());

            if (startTimeMillis == 0 || span.getStartTimeMillis() < startTimeMillis)
                startTimeMillis = span.getStartTimeMillis();
            if (span.getEndTimeMillis() > endTimeMillis)
                endTimeMillis = span.getEndTimeMillis();
        }

        return spans;
    }
}
