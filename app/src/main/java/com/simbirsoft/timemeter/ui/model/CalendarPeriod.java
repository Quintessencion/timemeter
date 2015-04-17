package com.simbirsoft.timemeter.ui.model;


import android.os.Parcelable;
import android.os.Parcel;
import com.google.common.collect.Lists;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarPeriod implements Parcelable {

    public static final Parcelable.Creator<CalendarPeriod> CREATOR
            = new Parcelable.Creator<CalendarPeriod>() {
        public CalendarPeriod createFromParcel(Parcel in) {
            return new CalendarPeriod(in);
        }

        public CalendarPeriod[] newArray(int size) {
            return new CalendarPeriod[size];
        }
    };

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM yyyy");
    private static final int MONTHS_IN_YEAR = 12;

    private final Calendar mStartDate = Calendar.getInstance();
    private final Calendar mEndDate = Calendar.getInstance();
    private final Calendar mPeriodStartMillis = Calendar.getInstance();
    private final Calendar mPeriodEndMillis = Calendar.getInstance();
    private final Calendar mFilterDateMillis = Calendar.getInstance();

    public CalendarPeriod() {

    }

    private CalendarPeriod(Parcel in) {
        mStartDate.setTimeInMillis(in.readLong());
        mEndDate.setTimeInMillis(in.readLong());
        mPeriodStartMillis.setTimeInMillis(in.readLong());
        mPeriodEndMillis.setTimeInMillis(in.readLong());
        mFilterDateMillis.setTimeInMillis(in.readLong());
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

    public String getPeriodString() {
        if (mStartDate.getTimeInMillis() == 0 || mEndDate.getTimeInMillis() == 0) return "";
        Date date = (mStartDate.get(Calendar.MONTH) != mEndDate.get(Calendar.MONTH) &&
                mEndDate.get(Calendar.DAY_OF_MONTH) >= 4) ? mEndDate.getTime() : mStartDate.getTime();
        return DATE_FORMAT.format(date);
    }


    public List<String> getTestStrings() {
        ArrayList<String> list = Lists.newArrayList();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, 20);
        for (int i = 0; i < MONTHS_IN_YEAR; i++) {
            calendar.set(Calendar.MONTH, i);
            list.add(DATE_FORMAT.format(calendar.getTime()));
        }
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(mStartDate.getTimeInMillis());
        parcel.writeLong(mEndDate.getTimeInMillis());
        parcel.writeLong(mPeriodStartMillis.getTimeInMillis());
        parcel.writeLong(mPeriodEndMillis.getTimeInMillis());
        parcel.writeLong(mFilterDateMillis.getTimeInMillis());
    }
}
