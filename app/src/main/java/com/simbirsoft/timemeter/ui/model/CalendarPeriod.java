package com.simbirsoft.timemeter.ui.model;


import com.google.common.collect.Lists;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarPeriod {
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("d");
    private static final SimpleDateFormat DAY_MONTH_FORMAT = new SimpleDateFormat("d MMM");
    private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");
    private static final int MONTHS_IN_YEAR = 12;

    private final Calendar mStartDate;
    private final Calendar mEndDate;
    private final Calendar mPeriodStartMillis;
    private final Calendar mPeriodEndMillis;
    private final Calendar mFilterDateMillis;

    public CalendarPeriod() {
        mStartDate = Calendar.getInstance();
        mEndDate = Calendar.getInstance();
        mPeriodStartMillis = Calendar.getInstance();
        mPeriodEndMillis = Calendar.getInstance();
        mFilterDateMillis = Calendar.getInstance();
    }

    public long getPeriodStartMillis() {
        return mPeriodStartMillis.getTimeInMillis();
    }

    public void setPeriodStartMillis(long millis) {
        mPeriodStartMillis.setTimeInMillis(millis);
    }

    public long getPeriodEndMillis() {
        return mPeriodEndMillis.getTimeInMillis();
    }

    public void setPeriodEndMillis(long millis) {
        mPeriodEndMillis.setTimeInMillis(millis);
    }

    public long getFilterDateMillis() {
        return mFilterDateMillis.getTimeInMillis();
    }

    public void setFilterDateMillis(long millis) {
        mFilterDateMillis.setTimeInMillis(millis);
    }

    public Date getStartDate() {
        return mStartDate.getTime();
    }

    public void setStartDate(Date startDate) {
        mStartDate.setTime(startDate);
    }

    public Date getEndDate() {
        return mEndDate.getTime();
    }

    public void setEndDate(Date endDate) {
        mEndDate.setTime(endDate);
    }

    public String getPeriodFirstString() {
        if (mStartDate.getTimeInMillis() == 0 || mEndDate.getTimeInMillis() == 0) return "";
        if (mStartDate.get(Calendar.YEAR) == mEndDate.get(Calendar.YEAR) &&
                mStartDate.get(Calendar.MONTH) == mEndDate.get(Calendar.MONTH)) {
            return String.format("%s - %s", DAY_FORMAT.format(mStartDate.getTime()), DAY_MONTH_FORMAT.format(mEndDate.getTime()));
        }
        return String.format("%s - %s", DAY_MONTH_FORMAT.format(mStartDate.getTime()), DAY_MONTH_FORMAT.format(mEndDate.getTime()));
    }

    public String getPeriodSecondString() {
        if (mEndDate.getTimeInMillis() == 0) return "";
        return YEAR_FORMAT.format(mEndDate.getTime());
    }

    public List<String> getFirstTestString() {
        ArrayList<String> list = Lists.newArrayList();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, 20);
        for (int i = 0; i < MONTHS_IN_YEAR; i++) {
            calendar.set(Calendar.MONTH, i);
            list.add(String.format("%s - %s", DAY_MONTH_FORMAT.format(calendar.getTime()), DAY_MONTH_FORMAT.format(calendar.getTime())));
        }
        return list;
    }

    public List<String> getSecondTestString() {
        ArrayList<String> list = Lists.newArrayList();
        list.add(YEAR_FORMAT.format(new Date()));
        return list;
    }

    public boolean canMoveNext() {
        if (mPeriodEndMillis.getTimeInMillis() == 0 || mEndDate.getTimeInMillis() == 0) return true;
        return mPeriodEndMillis.compareTo(mEndDate) > 0;
    }

    public boolean canMovePrev() {
        if (mPeriodStartMillis.getTimeInMillis() == 0 || mStartDate.getTimeInMillis() == 0) return true;
        return mPeriodStartMillis.compareTo(mStartDate) < 0;
    }

    public void moveNext() {
        mStartDate.add(Calendar.WEEK_OF_YEAR, 1);
        mEndDate.add(Calendar.WEEK_OF_YEAR, 1);
    }

    public void movePrev() {
        mStartDate.add(Calendar.WEEK_OF_YEAR, -1);
        mEndDate.add(Calendar.WEEK_OF_YEAR, -1);
    }
}
