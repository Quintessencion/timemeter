package com.simbirsoft.timemeter.ui.model;


import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.Date;

public abstract class TaskActivityItem {
    @IntDef({DATE_ITEM_TYPE, SPANS_ITEM_TYPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ItemType {}
    
    public static final int DATE_ITEM_TYPE = 0;
    public static final int SPANS_ITEM_TYPE = 1;

    private final Calendar mDate;

    @ItemType
    public abstract int getItemType();

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
