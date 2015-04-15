package com.simbirsoft.timemeter.ui.model;


public class CalendarData {
    private  ActivityCalendar mActivityCalendar;
    private CalendarPeriod mCalendarPeriod;

    public CalendarData(ActivityCalendar activityCalendar, CalendarPeriod calendarPeriod) {
        mActivityCalendar = activityCalendar;
        mCalendarPeriod = calendarPeriod;
    }

    public ActivityCalendar getActivityCalendar() {
        return mActivityCalendar;
    }

    public void setActivityCalendar(ActivityCalendar activityCalendar) {
        mActivityCalendar = activityCalendar;
    }

    public CalendarPeriod getCalendarPeriod() {
        return mCalendarPeriod;
    }

    public void setCalendarPeriod(CalendarPeriod calendarPeriod) {
        mCalendarPeriod = calendarPeriod;
    }
}
