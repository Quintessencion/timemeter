package com.simbirsoft.timemeter.ui.util;

import java.util.Calendar;

public final class TimeUtils {

    public static final long MILLIS_IN_HOUR = 3600000;

    public static long getDayStartMillis(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static long getDayStartMillis(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);

        return getDayStartMillis(c);
    }

    public static long tomorrowStart() {
        final Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTimeInMillis(System.currentTimeMillis());
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        tomorrow.set(
                tomorrow.get(Calendar.YEAR),
                tomorrow.get(Calendar.MONTH),
                tomorrow.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0);

        return tomorrow.getTimeInMillis();
    }

    public static long getWeekFirstDayStartMillis(Calendar calendar) {
        calendar.setTimeInMillis(getDayStartMillis(calendar));
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return calendar.getTimeInMillis();
    }

    public static long getWeekFirstDayStartMillis(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return getWeekFirstDayStartMillis(c);
    }

    public static long getWeekLastDayStartMillis(Calendar calendar) {
        calendar.setTimeInMillis(getWeekFirstDayStartMillis(calendar));
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        return calendar.getTimeInMillis();
    }

    public static long getWeekLastDayStartMillis(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return getWeekLastDayStartMillis(c);
    }

    public static long getDayEndMillis(Calendar calendar) {
        calendar.setTimeInMillis(getDayStartMillis(calendar));
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTimeInMillis();
    }

    public static long getDayEndMillis(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);

        return getDayEndMillis(c);
    }


    public static long hoursToMillis(int hours) {
        return hours * MILLIS_IN_HOUR;
    }
}
