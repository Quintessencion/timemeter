package com.simbirsoft.timemeter.ui.model;


import com.simbirsoft.timemeter.ui.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskActivityDateItem extends TaskActivityItem {
    private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("LLLL yyyy");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("LLLL");

    @Override
    public int getItemType() {
        return DATE_ITEM_TYPE;
    }

    public String getDateString() {
        return (TimeUtils.isCurrentYear(getDateMillis()))
                ? DATE_FORMAT.format(getDate()) : FULL_DATE_FORMAT.format(getDate());

    }
}
