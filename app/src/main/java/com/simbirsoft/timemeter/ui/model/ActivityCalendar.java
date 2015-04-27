package com.simbirsoft.timemeter.ui.model;

import android.content.res.Resources;

import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.util.ColorSets;
import com.simbirsoft.timemeter.ui.util.TimeSpanDaysSplitter;
import com.simbirsoft.timemeter.ui.util.TimeUtils;

import org.slf4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class ActivityCalendar {

    private static final Logger LOG = LogFactory.getLogger(ActivityCalendar.class);

    private static final SimpleDateFormat WEEK_DAY_FORMAT = new SimpleDateFormat("EE");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d");

    private static final int START_HOUR_DEFAULT = 0;
    private static final int END_HOUR_DEFAULT = 24;

    private final Calendar mStartDate;
    private final Calendar mEndDate;
    private final Calendar mBufferCalendar;
    private int mStartHour = START_HOUR_DEFAULT;
    private int mEndHour = END_HOUR_DEFAULT;
    private final List<Date> mDays;
    private final Multimap<Integer, TaskTimeSpan> mDailyActivity;

    public ActivityCalendar() {
        mDailyActivity = Multimaps.newListMultimap(Maps.newHashMap(), Lists::newArrayList);
        mDays = Lists.newArrayList();
        mStartDate = Calendar.getInstance();
        mEndDate = Calendar.getInstance();
        mBufferCalendar = Calendar.getInstance();
    }


    public static List<TaskTimeSpan> splitTimeSpansByDays(List<TaskTimeSpan> input) {
        final List<TaskTimeSpan> result = Lists.newArrayListWithCapacity(input.size());

        final Calendar cal1 = Calendar.getInstance();
        final Calendar cal2 = Calendar.getInstance();

        final List<TaskTimeSpan> splitSpans = Lists.newArrayList();

        for (TaskTimeSpan span : input) {
            TimeSpanDaysSplitter.splitTimeSpanByDays(cal1, cal2, span, splitSpans);
            result.addAll(splitSpans);
            splitSpans.clear();
        }

        return result;
    }

    private void updateDays() {
        mDays.clear();

        int yearStart;
        int dayStart;
        int yearEnd;
        int dayEnd;

        Calendar start = Calendar.getInstance();
        start.setTime(mStartDate.getTime());
        yearEnd = mEndDate.get(Calendar.YEAR);
        dayEnd = mEndDate.get(Calendar.DAY_OF_YEAR);
        do {
            Date day = new Date(TimeUtils.getDayStartMillis(start));
            mDays.add(day);
            LOG.debug("added date to calendar: '{}'", day);

            yearStart = start.get(Calendar.YEAR);
            dayStart = start.get(Calendar.DAY_OF_YEAR);

            start.add(Calendar.DAY_OF_YEAR, 1);

        } while (yearStart < yearEnd || (yearStart == yearEnd && dayStart < dayEnd));
    }

    public String getWeekDayLabel(int dayIndex) {
        return WEEK_DAY_FORMAT.format(getDay(dayIndex));
    }

    public String getDateLabel(int dayIndex) {
        return DATE_FORMAT.format(getDay(dayIndex));
    }

    public int getDateLabelColor(Resources res, int dayIndex) {
        mBufferCalendar.setTime(new Date());
        int dayOfYear = mBufferCalendar.get(Calendar.DAY_OF_YEAR);
        mBufferCalendar.setTime(getDay(dayIndex));

        if (mBufferCalendar.get(Calendar.DAY_OF_YEAR) == dayOfYear) {
            return res.getColor(R.color.primary);
        }

        int dayOfWeek = mBufferCalendar.get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek == Calendar.SATURDAY
                || dayOfWeek == Calendar.SUNDAY) {

            return res.getColor(R.color.accentPrimary);
        }

        return res.getColor(R.color.calendar_date_text);
    }

    public String getHourLabel(int hourIndex) {
        return String.format("%02d", getHour(hourIndex));
    }

    public List<Date> getDays() {
        return mDays;
    }

    public Date getDay(int dayIndex) {
        Preconditions.checkElementIndex(dayIndex, mDays.size(), "day is out of days range");

        return mDays.get(dayIndex);
    }

    public int getHour(int hourIndex) {
        Preconditions.checkElementIndex(hourIndex, getHoursCount(), "hour is out of hours range");

        return mStartHour + hourIndex;
    }

    public List<TaskTimeSpan> getActivityForDayIndex(int dayIndex) {
        if (dayIndex < 0 || dayIndex >= getDaysCount()) {
            return Collections.emptyList();
        }

        List<TaskTimeSpan> result = (List<TaskTimeSpan>) mDailyActivity.get(dayIndex);

        return Collections.unmodifiableList(result);
    }

    public void setDailyActivity(List<TaskTimeSpan> spans) {
        mDailyActivity.clear();

        Collections.sort(spans, (item1, item2) ->
                (int) (item1.getStartTimeMillis() - item2.getStartTimeMillis()));

        if (spans.isEmpty()) {
            mStartHour = START_HOUR_DEFAULT;
            mEndHour = END_HOUR_DEFAULT;
            return;
        }

        List<TaskTimeSpan> splitSpans = splitTimeSpansByDays(spans);
        Calendar calendar = Calendar.getInstance();
        for (TaskTimeSpan span : splitSpans) {
            calendar.setTimeInMillis(span.getStartTimeMillis());
            long dayStartMillis = TimeUtils.getDayStartMillis(calendar);
            int dayIndex = getDayIndex(dayStartMillis);

            if (dayIndex < 0) {
                LOG.debug("activity not added to calendar");
                continue;
            }

            mDailyActivity.put(dayIndex, span);
            LOG.debug("activity added to calendar day '{}'; duration: '{}'", dayIndex, span.getDuration());
        }
    }

    public int getDaysCount() {
        return mDays.size();
    }

    public int getHoursCount() {
        return mEndHour - mStartHour;
    }

    public Date getStartDate() {
        return mStartDate.getTime();
    }

    public void setStartDate(Date startDate) {
        mStartDate.setTime(startDate);
        updateDays();
    }

    public Date getEndDate() {
        return mEndDate.getTime();
    }

    public void setEndDate(Date endDate) {
        mEndDate.setTime(endDate);
        updateDays();
    }

    public int getDayIndex(long dayStartTimeMillis) {
        int index = 0;
        for (Date date : mDays) {
            if (date.getTime() == dayStartTimeMillis) {
                return index;
            }

            index++;
        }

        return -1;
    }

    public long getDayStartMillis(int dayIndex) {
        return mDays.get(dayIndex).getTime();
    }

    public int getTimeSpanColor(TaskTimeSpan span) {
        return ColorSets.getTaskColor(span.getTaskId());
    }

    public long getCellStartMillis(WeekCalendarCell cell) {
        mBufferCalendar.setTime(getDay(cell.getDayIndex()));
        mBufferCalendar.add(Calendar.HOUR_OF_DAY, mStartHour + cell.getHourIndex());
        return mBufferCalendar.getTimeInMillis();
    }

    public List<TaskTimeSpan> getActivitiesInCell(WeekCalendarCell cell) {
        ArrayList<TaskTimeSpan> result = Lists.newArrayList();
        List<TaskTimeSpan> spans = getActivityForDayIndex(cell.getDayIndex());
        long cellStart = getCellStartMillis(cell);
        mBufferCalendar.setTimeInMillis(cellStart);
        mBufferCalendar.add(Calendar.HOUR_OF_DAY, 1);
        long cellEnd = mBufferCalendar.getTimeInMillis();
        for (TaskTimeSpan span : spans) {
            if (span.getStartTimeMillis() < cellEnd && span.getEndTimeMillis() >= cellStart) {
                result.add(span);
            }
        }
        return result;
    }
}
