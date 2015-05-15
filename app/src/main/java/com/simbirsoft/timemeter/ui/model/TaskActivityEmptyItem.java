package com.simbirsoft.timemeter.ui.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TaskActivityEmptyItem extends TaskActivityItem {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd");
    private static final SimpleDateFormat WEEK_DAY_FORMAT = new SimpleDateFormat("EE");

    @Override
    public int getItemType() {
        return EMPTY_ITEM_TYPE;
    }

    public String getDateString() {
        return DATE_FORMAT.format(getDate());
    }

    public String getWeekDayString() {
        return WEEK_DAY_FORMAT.format(getDate()).toUpperCase();
    }
}
