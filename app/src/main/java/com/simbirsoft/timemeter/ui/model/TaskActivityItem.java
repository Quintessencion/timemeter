package com.simbirsoft.timemeter.ui.model;


import java.util.Calendar;
import java.util.Date;

public abstract class TaskActivityItem {
    private final Calendar mDate;

    public TaskActivityItem() {
        mDate = Calendar.getInstance();
    }

    public long getDateMillis() {
        return  mDate.getTimeInMillis();
    }

    public void setDateMillis(long millis) {
        mDate.setTimeInMillis(millis);
    }

    public Date getDate() {
        return  mDate.getTime();
    }

    public void setDate(Date date) {
        mDate.setTime(date);
    }
}
