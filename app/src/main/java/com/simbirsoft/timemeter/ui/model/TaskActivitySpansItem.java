package com.simbirsoft.timemeter.ui.model;


import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class TaskActivitySpansItem extends TaskActivityItem {
    public static final String TEST_STRING = "00:00";

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

    public String getStartHourLabel(int index) {
        Preconditions.checkElementIndex(index, mList.size(), "index is out of items range");
        return getHourLabel(mList.get(index).getStartTimeMillis());
    }

    public String getEndHourLabel(int index) {
        Preconditions.checkElementIndex(index, mList.size(), "index is out of items range");
        return getHourLabel(mList.get(index).getEndTimeMillis());
    }

    public int getSpansCount() {
        return (mList == null) ? 0 : mList.size();
    }

    private String getHourLabel(long millis) {
        mCalendar.setTimeInMillis(millis);
        return String.format("%02d:%02d", mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
    }
}
