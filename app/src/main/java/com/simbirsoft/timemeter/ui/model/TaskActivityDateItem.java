package com.simbirsoft.timemeter.ui.model;


import java.text.SimpleDateFormat;

public class TaskActivityDateItem extends TaskActivityItem {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM yyyy");

    public TaskActivityDateItem() {
        super();
    }

    public String getDateString() {
        return  DATE_FORMAT.format(getDate());
    }
}
