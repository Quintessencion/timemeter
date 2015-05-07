package com.simbirsoft.timemeter.ui.util;


import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.model.TaskActivityDateItem;
import com.simbirsoft.timemeter.ui.model.TaskActivityEmptyItem;
import com.simbirsoft.timemeter.ui.model.TaskActivityItem;
import com.simbirsoft.timemeter.ui.model.TaskActivitySpansItem;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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

    public static List<TaskActivityItem> convertToTaskActivityItems(List<TaskTimeSpan> spans, boolean includeDateItems, boolean includeEmptyItems) {
        Collections.sort(spans, new Comparator<TaskTimeSpan>() {
            @Override
            public int compare(TaskTimeSpan lhs, TaskTimeSpan rhs) {
                long millis1 = lhs.getStartTimeMillis();
                long millis2 = rhs.getStartTimeMillis();
                return (millis1 == millis2) ? 0 : (millis1 > millis2) ? -1 : 1;
            }
        });
        List<TaskActivityItem> items = Lists.newArrayList();
        Calendar currentMonth = Calendar.getInstance();
        Calendar currentDay = Calendar.getInstance();
        currentMonth.setTimeInMillis(0);
        currentDay.setTimeInMillis(0);

        final Calendar cal1 = Calendar.getInstance();
        final Calendar cal2 = Calendar.getInstance();
        final List<TaskTimeSpan> splitSpans = Lists.newArrayList();
        final List<TaskTimeSpan> dailySpans = Lists.newArrayList();

        for(TaskTimeSpan span : spans) {
            TimeSpanDaysSplitter.splitTimeSpanByDays(cal1, cal2, span, splitSpans);
            Collections.reverse(splitSpans);
            for (TaskTimeSpan span1 : splitSpans) {
                if (!isInDay(currentDay, cal1, span1)) {
                    createListItem(items, dailySpans, currentDay);
                    currentDay.setTimeInMillis(TimeUtils.getDayStartMillis(span1.getStartTimeMillis()));
                    if (!isInMonth(currentMonth, cal1, span1)) {
                        currentMonth.setTimeInMillis(TimeUtils.getDayStartMillis(span1.getStartTimeMillis()));
                        currentMonth.set(Calendar.DAY_OF_MONTH, 1);
                        if (includeDateItems) {
                            createDateItem(items, currentMonth);
                        }
                    }
                }
                dailySpans.add(span1);
            }
            splitSpans.clear();
        }
        createListItem(items, dailySpans, currentDay);
        return items;
    }

    private static void createListItem(List<TaskActivityItem> items, List<TaskTimeSpan> dailySpans, Calendar currentDay) {
        if (!dailySpans.isEmpty()) {
            Collections.reverse(dailySpans);
            TaskActivitySpansItem item = new TaskActivitySpansItem();
            item.setDate(currentDay.getTime());
            item.setList(Lists.newArrayList(dailySpans));
            items.add(item);
            dailySpans.clear();
        }
    }

    private static void createDateItem(List<TaskActivityItem> items, Calendar currentMonth) {
        TaskActivityDateItem item = new TaskActivityDateItem();
        item.setDate(currentMonth.getTime());
        items.add(item);
    }

    private static void createEmptyItem(List<TaskActivityItem> items, Calendar currentDay) {
        TaskActivityEmptyItem item = new TaskActivityEmptyItem();
        item.setDate(currentDay.getTime());
        items.add(item);
    }

    private static boolean isInDay(Calendar dayCalendar, Calendar calendar, TaskTimeSpan span) {
        calendar.setTimeInMillis(span.getStartTimeMillis());
        return (dayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                dayCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR));
    }

    private static boolean isInMonth(Calendar monthCalendar, Calendar calendar, TaskTimeSpan span) {
        calendar.setTimeInMillis(span.getStartTimeMillis());
        return (monthCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                monthCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH));
    }
}
