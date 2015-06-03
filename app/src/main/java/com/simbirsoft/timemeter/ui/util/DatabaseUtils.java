package com.simbirsoft.timemeter.ui.util;

import com.simbirsoft.timemeter.db.model.TaskTimeSpan;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class DatabaseUtils {

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

        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND, 0);
        c.add(Calendar.WEEK_OF_YEAR, -weeks);

        long shift = c.getTimeInMillis() - startTimeMillis;

        for (TaskTimeSpan span : spans) {
            span.setStartTimeMillis(span.getStartTimeMillis() + shift);
            span.setEndTimeMillis(span.getEndTimeMillis() + shift);
        }

        return spans;
    }
}
