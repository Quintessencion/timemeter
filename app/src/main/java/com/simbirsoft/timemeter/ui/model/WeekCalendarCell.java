package com.simbirsoft.timemeter.ui.model;

public class WeekCalendarCell {
    private int dayIndex = -1;
    private int hourIndex = -1;

    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public int getHourIndex() {
        return hourIndex;
    }

    public void setHourIndex(int hourIndex) {
        this.hourIndex = hourIndex;
    }

    public boolean isEqual(WeekCalendarCell cell) {
        return dayIndex == cell.dayIndex && hourIndex == cell.hourIndex;
    }

    public WeekCalendarCell() {

    }

    public WeekCalendarCell(int dayIndex, int hourIndex) {
        this.dayIndex = dayIndex;
        this.hourIndex = hourIndex;
    }

    public boolean equals(WeekCalendarCell cell) {
        return dayIndex == cell.dayIndex && hourIndex == cell.hourIndex;
    }
}
