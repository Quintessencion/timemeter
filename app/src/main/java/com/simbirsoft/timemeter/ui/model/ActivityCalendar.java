package com.simbirsoft.timemeter.ui.model;

import android.content.res.Resources;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.util.TimeUtils;

import org.slf4j.Logger;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ActivityCalendar {

    private static final Logger LOG = LogFactory.getLogger(ActivityCalendar.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EE");

    private static final int START_HOUR_DEFAULT = 0;
    private static final int END_HOUR_DEFAULT = 24;

    private final Calendar mStartDate;
    private final Calendar mEndDate;
    private final Calendar mPeriodStart;
    private final Calendar mPeriodEnd;
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
        mPeriodStart = Calendar.getInstance();
        mPeriodEnd = Calendar.getInstance();
        mBufferCalendar = Calendar.getInstance();
    }

    // TODO: test multi-day activity split
    public static List<TaskTimeSpan> splitTimeSpansByDays(List<TaskTimeSpan> input) {
        final List<TaskTimeSpan> result = Lists.newArrayListWithCapacity(input.size());

        final Calendar cal1 = Calendar.getInstance();
        final Calendar cal2 = Calendar.getInstance();

        final List<TaskTimeSpan> splitSpans = Lists.newArrayList();

        for (TaskTimeSpan span : input) {
            splitTimeSpanByDays(cal1, cal2, span, splitSpans);
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
        do {
            Date day = new Date(TimeUtils.getDayStartMillis(start));
            mDays.add(day);
            LOG.debug("added date to calendar: '{}'", day);

            yearStart = start.get(Calendar.YEAR);
            dayStart = start.get(Calendar.DAY_OF_YEAR);
            yearEnd = mEndDate.get(Calendar.YEAR);
            dayEnd = mEndDate.get(Calendar.DAY_OF_YEAR);

            start.add(Calendar.DAY_OF_YEAR, 1);

        } while (yearStart < yearEnd || (yearStart == yearEnd && dayStart < dayEnd));
    }

    public String getDateLabel(int dayIndex) {
        return DATE_FORMAT.format(getDay(dayIndex)).toUpperCase();
    }

    public int getDateLabelColor(Resources res, int dayIndex) {
        mBufferCalendar.setTime(getDay(dayIndex));
        int dayOfWeek = mBufferCalendar.get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek == Calendar.SATURDAY
                || dayOfWeek == Calendar.SUNDAY) {

            return res.getColor(R.color.accentPrimary);
        }

        return res.getColor(R.color.darkGrey);
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

    public void setPeriodStart(long millis) {
        mPeriodStart.setTimeInMillis(millis);
    }

    public void setPeriodEnd(long millis) {
        mPeriodEnd.setTimeInMillis(millis);
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

    private static void splitTimeSpanByDays(Calendar calendar1, Calendar calendar2,
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

            calendar1.add(Calendar.DAY_OF_YEAR, 1);

            currentTimeMillis = TimeUtils.getDayStartMillis(calendar1) - 1;
            if (currentTimeMillis > spanEndTimeMillis) {
                currentTimeMillis = spanEndTimeMillis;
            }
            calendar1.setTimeInMillis(currentTimeMillis);
            newSpan.setEndTimeMillis(currentTimeMillis);
            container.add(newSpan);

            yearStart = calendar1.get(Calendar.YEAR);
            dayStart = calendar1.get(Calendar.DAY_OF_YEAR);
        }
    }
}
