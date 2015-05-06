package com.simbirsoft.timemeter.ui.util;


import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.log.LogFactory;

import java.util.Calendar;
import java.util.List;

public class TimeSpanDaysSplitter {
    public static void splitTimeSpanByDays(Calendar calendar1, Calendar calendar2,
                                            TaskTimeSpan span, List<TaskTimeSpan> container) {

        final long spanEndTimeMillis = span.getEndTimeMillis();

        calendar1.setTimeInMillis(span.getStartTimeMillis());
        calendar2.setTimeInMillis(span.getEndTimeMillis());

        int yearStart = calendar1.get(Calendar.YEAR);
        int dayStart = calendar1.get(Calendar.DAY_OF_YEAR);
        final int yearEnd = calendar2.get(Calendar.YEAR);
        final int dayEnd = calendar2.get(Calendar.DAY_OF_YEAR);

        if (yearStart == yearEnd && dayStart == dayEnd) {
            // Span covers single day
            container.add(span);
            return;
        }

        long currentTimeMillis = span.getStartTimeMillis();

        while (yearStart < yearEnd || (yearStart == yearEnd && dayStart <= dayEnd)) {
            TaskTimeSpan newSpan = new TaskTimeSpan();
            newSpan.setId(span.getId());
            newSpan.setDescription(span.getDescription());
            newSpan.setStartTimeMillis(currentTimeMillis);
            newSpan.setTaskId(span.getTaskId());

            calendar1.add(Calendar.DAY_OF_YEAR, 1);

            currentTimeMillis = TimeUtils.getDayStartMillis(calendar1);
            long newSpanEndTime = currentTimeMillis - 1;
            if (newSpanEndTime > spanEndTimeMillis) {
                newSpanEndTime = spanEndTimeMillis;
            }
            calendar1.setTimeInMillis(currentTimeMillis);
            if (newSpan.getStartTimeMillis() != newSpanEndTime) {
                newSpan.setEndTimeMillis(newSpanEndTime);
                container.add(newSpan);
            }
            yearStart = calendar1.get(Calendar.YEAR);
            dayStart = calendar1.get(Calendar.DAY_OF_YEAR);
        }
    }
}
