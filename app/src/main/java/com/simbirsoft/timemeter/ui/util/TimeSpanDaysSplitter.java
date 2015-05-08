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
import java.util.Date;
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

    public static List<TaskActivityItem> convertToTaskActivityItems(List<TaskTimeSpan> spans) {
        sortSpans(spans);
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
                    currentDay.setTimeInMillis(span1.getStartTimeMillis());
                    if (!isInMonth(currentMonth, cal1, span1)) {
                        currentMonth.setTimeInMillis(span1.getStartTimeMillis());
                        createDateItem(items, currentMonth);
                    }
                }
                dailySpans.add(span1);
            }
            splitSpans.clear();
        }
        createListItem(items, dailySpans, currentDay);
        return items;
    }

    public static List<TaskActivityItem> convertToTaskRecentActivityItems(List<TaskTimeSpan> spans) {
        List<TaskActivityItem> items = Lists.newArrayList();
        if (spans.isEmpty()) return items;
        sortSpans(spans);
        Calendar currentDay = Calendar.getInstance();
        currentDay.setTimeInMillis(0);

        final Calendar cal1 = Calendar.getInstance();
        final Calendar cal2 = Calendar.getInstance();
        final List<TaskTimeSpan> splitSpans = Lists.newArrayList();
        final List<TaskTimeSpan> dailySpans = Lists.newArrayList();
        cal1.setTime(new Date());
        final boolean includeDateItems = !isInMonth(cal1, cal2, spans.get(spans.size() - 1));

        for(TaskTimeSpan span : spans) {
            TimeSpanDaysSplitter.splitTimeSpanByDays(cal1, cal2, span, splitSpans);
            Collections.reverse(splitSpans);
            for (TaskTimeSpan span1 : splitSpans) {
                if (!isInDay(currentDay, cal1, span1)) {
                    createListItem(items, dailySpans, currentDay);
                    addItemsBetweenSpans(items, currentDay.getTimeInMillis(), span1.getStartTimeMillis(), cal1, cal2, includeDateItems);
                    currentDay.setTimeInMillis(span1.getStartTimeMillis());
                }
                dailySpans.add(span1);
            }
            splitSpans.clear();
        }
        createListItem(items, dailySpans, currentDay);
        return items;
    }

    private static void sortSpans(List<TaskTimeSpan> spans) {
        Collections.sort(spans, new Comparator<TaskTimeSpan>() {
            @Override
            public int compare(TaskTimeSpan lhs, TaskTimeSpan rhs) {
                long millis1 = lhs.getStartTimeMillis();
                long millis2 = rhs.getStartTimeMillis();
                return (millis1 == millis2) ? 0 : (millis1 > millis2) ? -1 : 1;
            }
        });
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

    private static boolean isInDay(Calendar dayCalendar, Calendar calendar, TaskTimeSpan span) {
        calendar.setTimeInMillis(span.getStartTimeMillis());
        return (dayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                dayCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR));
    }

    private static boolean isInMonth(Calendar monthCalendar, Calendar calendar, TaskTimeSpan span) {
        calendar.setTimeInMillis(span.getStartTimeMillis());
        return isInMonth(monthCalendar, calendar);
    }

    private static boolean isInMonth(Calendar monthCalendar, Calendar calendar) {
        return (monthCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                monthCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH));
    }

    private static void addItemsBetweenSpans(List<TaskActivityItem> items, long date1, long date2,
                                      Calendar cal1, Calendar cal2, boolean includeDateItems) {
        Calendar currentMonth = Calendar.getInstance();
        if (items.isEmpty()) {
            cal1.setTime(new Date());
            currentMonth.setTime(cal1.getTime());
            if (includeDateItems) {
                createDateItem(items, cal1);
            }
        } else {
            cal1.setTimeInMillis(date1);
            currentMonth.setTimeInMillis(date1);
            cal1.add(Calendar.DAY_OF_YEAR, -1);
        }
        cal2.setTimeInMillis(date2);
        cal2.add(Calendar.DAY_OF_YEAR, 1);
        int yearStart = cal1.get(Calendar.YEAR);
        int dayStart = cal1.get(Calendar.DAY_OF_YEAR);
        final int yearEnd = cal2.get(Calendar.YEAR);
        final int dayEnd = cal2.get(Calendar.DAY_OF_YEAR);
        while (yearStart > yearEnd || (yearStart == yearEnd && dayStart >= dayEnd)) {
            if (includeDateItems && !isInMonth(currentMonth, cal1)) {
                currentMonth.setTimeInMillis(cal1.getTimeInMillis());
                createDateItem(items, currentMonth);
            }
            TaskActivityEmptyItem item = new TaskActivityEmptyItem();
            item.setDate(cal1.getTime());
            items.add(item);
            cal1.add(Calendar.DAY_OF_YEAR, -1);
            yearStart = cal1.get(Calendar.YEAR);
            dayStart = cal1.get(Calendar.DAY_OF_YEAR);
        }
    }
}
