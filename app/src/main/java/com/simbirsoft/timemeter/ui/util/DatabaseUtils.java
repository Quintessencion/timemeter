package com.simbirsoft.timemeter.ui.util;

import android.content.Context;

import com.google.common.io.Closeables;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.DemoTask;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTag;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.persist.XmlTag;
import com.simbirsoft.timemeter.persist.XmlTagRef;
import com.simbirsoft.timemeter.persist.XmlTask;
import com.simbirsoft.timemeter.persist.XmlTaskList;
import com.simbirsoft.timemeter.persist.XmlTaskListReader;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
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
                List<TaskTimeSpan> spans = xmlTask.getTaskActivity();
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

    public static List<TaskTimeSpan> actualizeTaskActivities(List<TaskTimeSpan> spans) {
        long startTimeMillis = 0;
        long endTimeMillis = 0;

        for (TaskTimeSpan span : spans) {
            if (startTimeMillis == 0 || span.getStartTimeMillis() < startTimeMillis)
                startTimeMillis = span.getStartTimeMillis();
            if (span.getEndTimeMillis() > endTimeMillis)
                endTimeMillis = span.getEndTimeMillis();
        }

        long duration = endTimeMillis - startTimeMillis;
        int weeks = 1 + ((int) TimeUnit.MILLISECONDS.toDays(duration) / 7);
        int firstActivityStartHour = (int) TimeUnit.MILLISECONDS.toHours(startTimeMillis) % 24;
        int firstActivityStartMinute = (int) TimeUnit.MILLISECONDS.toMinutes(startTimeMillis) % 60;
        int firstActivityStartSecond = (int) TimeUnit.MILLISECONDS.toSeconds(startTimeMillis) % 60;

        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, firstActivityStartHour);
        c.set(Calendar.MINUTE, firstActivityStartMinute);
        c.set(Calendar.SECOND, firstActivityStartSecond);
        c.add(Calendar.WEEK_OF_YEAR, -weeks);

        long shift = c.getTimeInMillis() - startTimeMillis;

        for (TaskTimeSpan span : spans) {
            span.setStartTimeMillis(span.getStartTimeMillis() + shift);
            span.setEndTimeMillis(span.getEndTimeMillis() + shift);
        }

        return spans;
    }
}
