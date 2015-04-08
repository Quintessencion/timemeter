package com.simbirsoft.timemeter.ui.util;

import java.util.Calendar;

public final class TimeUtils {

    private static final long MILLIS_IN_HOUR = 3600000;

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

    public static long getWeekStartMillis(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        return calendar.getTimeInMillis();
    }

    public static long getWeekStartMillis(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return getWeekStartMillis(c);
    }

    public static long getWeekEndMillis(Calendar calendar) {
        calendar.setTimeInMillis(getWeekStartMillis(calendar));
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        return calendar.getTimeInMillis();
    }

    public static long getWeekEndMillis(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return getWeekEndMillis(c);
    }

    public static long hoursToMillis(int hours) {
        return hours * MILLIS_IN_HOUR;
    }
}
