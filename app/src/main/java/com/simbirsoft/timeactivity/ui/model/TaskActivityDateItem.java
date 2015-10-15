package com.simbirsoft.timeactivity.ui.model;


import com.simbirsoft.timeactivity.ui.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TaskActivityDateItem extends TaskActivityItem {
    private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("LLLL yyyy");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("LLLL");

    private final Calendar mCalendar;

    public TaskActivityDateItem() {
        mCalendar = Calendar.getInstance();
    }

    @Override
    public int getItemType() {
        return DATE_ITEM_TYPE;
    }

    public String getDateString() {
        return (TimeUtils.isCurrentYear(getDateMillis(), mCalendar))
                ? DATE_FORMAT.format(getDate()) : FULL_DATE_FORMAT.format(getDate());

    }
}
