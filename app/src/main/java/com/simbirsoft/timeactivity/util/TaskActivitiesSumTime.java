package com.simbirsoft.timeactivity.util;


import com.simbirsoft.timeactivity.db.model.TaskTimeSpan;

import java.util.Calendar;
import java.util.List;

public class TaskActivitiesSumTime {

    private static final String HOUR_MINUTE = "%02d:%02d";
    private static final String HOUR_MINUTE_SECOND = "%02d:%02d:%02d";

    public static String getSumHoursMinute(long sum) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(sum);
        return String.format(HOUR_MINUTE, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    public static String getSumHoursMinuteSecond(long sum) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(sum);
        return String.format(HOUR_MINUTE_SECOND, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
    }
    public static long getSumTimeImMillis(List<TaskTimeSpan> spans) {
        long sum = 0;

        for (TaskTimeSpan span: spans) {
            sum += span.getEndTimeMillis() - span.getStartTimeMillis();
        }

        return sum;
    }
}
