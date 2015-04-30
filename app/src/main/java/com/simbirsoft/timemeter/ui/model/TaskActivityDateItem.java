package com.simbirsoft.timemeter.ui.model;


import java.text.SimpleDateFormat;

public class TaskActivityDateItem extends TaskActivityItem {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("LLLL yyyy");

    @Override
    public int getItemType() {
        return DATE_ITEM_TYPE;
    }

    public String getDateString() {
        return  DATE_FORMAT.format(getDate());
    }
}
