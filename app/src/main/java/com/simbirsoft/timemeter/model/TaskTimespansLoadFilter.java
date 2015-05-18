package com.simbirsoft.timemeter.model;


import com.simbirsoft.timemeter.ui.util.TimeUtils;

public class TaskTimespansLoadFilter {
    private long mStartDateMillis;
    private long mEndDateMillis;
    private Period mPeriod;

    public TaskTimespansLoadFilter startDateMillis(long dateMillis) {
        mStartDateMillis = dateMillis;

        return this;
    }

    public TaskTimespansLoadFilter endDateMillis(long dateMillis) {
        mEndDateMillis = dateMillis;

        return this;
    }

    public TaskTimespansLoadFilter period(Period period) {
        mPeriod = period;

        return this;
    }


    public long getStartDateMillis() {
        return mStartDateMillis;
    }

    public long getEndDateMillis() {
        return mEndDateMillis;
    }

    public Period getPeriod() {
        return mPeriod;
    }

    public void clear() {
        mPeriod = null;
        mStartDateMillis = 0;
        mEndDateMillis = 0;
    }

    public void getDateBounds(long[] bounds) {
        bounds[0] = 0;
        bounds[1] = 0;
        if (mStartDateMillis > 0) {
            bounds[0] = TimeUtils.getDayStartMillis(mStartDateMillis);
        }
        if (mPeriod == null || mPeriod == Period.ALL) {
            return;
        }
        if (mPeriod == Period.OTHER) {
            bounds[1] = TimeUtils.getDayEndMillis(mEndDateMillis);
        } else {
            bounds[1] = Period.getPeriodEnd(mPeriod, bounds[0]);
        }
    }
}
