package com.simbirsoft.timemeter.ui.model;


import android.content.Context;
import android.text.Html;

import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.ui.util.TimerTextFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class TaskActivitySpansItem extends TaskActivityItem {
    private static final char EM_DASH = 0x2014;
    private static final String TIME_TEST_STRING = String.format("00:00 %c 00:00", EM_DASH);
    private static final long DURATION_TEST_VALUE = (20 * 3600 + 20 * 60 + 20) * 1000;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d");
    private static final SimpleDateFormat WEEK_DAY_FORMAT = new SimpleDateFormat("EE");

    private List<TaskTimeSpan> mList;
    private final Calendar mCalendar;

    public TaskActivitySpansItem() {
        super();
        mCalendar = Calendar.getInstance();
    }

    public List<TaskTimeSpan> getList() {
        return (mList == null) ? Collections.<TaskTimeSpan>emptyList() : mList;
    }

    public void setList(List<TaskTimeSpan> list) {
        mList = list;
    }

    public String getDateString() {
        return DATE_FORMAT.format(getDate());
    }

    public String getWeekDayString() {
        return WEEK_DAY_FORMAT.format(getDate());
    }

    public String getSpanTimeTestLabel() {
        return TIME_TEST_STRING;
    }

    public String getSpanTimeLabel(int index) {
        Preconditions.checkElementIndex(index, mList.size(), "index is out of items range");
        TaskTimeSpan span = mList.get(index);
        return String.format("%s %c %s", getHourLabel(span.getStartTimeMillis()), EM_DASH, getHourLabel(span.getEndTimeMillis()));
    }

    public String getSpanDurationTestLabel(Context context) {
        return Html.fromHtml(TimerTextFormatter.formatTaskSpanText(
                context.getResources(), DURATION_TEST_VALUE)).toString();
    }

    public String getSpanDurationLabel(int index, Context context) {
        Preconditions.checkElementIndex(index, mList.size(), "index is out of items range");
        TaskTimeSpan span = mList.get(index);
        return Html.fromHtml(TimerTextFormatter.formatTaskSpanText(
                context.getResources(), span.getDuration())).toString();
    }

    public int getSpansCount() {
        return (mList == null) ? 0 : mList.size();
    }

    private String getHourLabel(long millis) {
        mCalendar.setTimeInMillis(millis);
        return String.format("%02d:%02d", mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
    }
}
