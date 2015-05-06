package com.simbirsoft.timemeter.ui.util;

import java.util.Calendar;
import java.util.Date;

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
        int days = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
        calendar.add(Calendar.DAY_OF_YEAR, (days >=0) ? -days : (-days - 7));
        return calendar.getTimeInMillis();
    }

    public static long getWeekFirstDayStartMillis(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return getWeekFirstDayStartMillis(c);
    }

    public static long getWeekLastDayStartMillis(Calendar calendar) {
        calendar.setTimeInMillis(getDayStartMillis(calendar));
        int days = Calendar.SUNDAY - calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DAY_OF_YEAR, (days >=0) ? days : (7 + days));
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

    public static boolean isCurrentYear(long millis, Calendar calendar) {
        calendar.setTimeInMillis(millis);
        int year = calendar.get(Calendar.YEAR);
        calendar.setTime(new Date());
        return calendar.get(Calendar.YEAR) == year;
    }

    public static boolean isCurrentDay(long millis, Calendar calendar) {
        calendar.setTimeInMillis(millis);
        int year = calendar.get(Calendar.YEAR);
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.setTime(new Date());
        return calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.DAY_OF_YEAR) == dayOfYear;
    }

    public static boolean isHoliday(long millis, Calendar calendar) {
        calendar.setTimeInMillis(millis);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }

    public static long hoursToMillis(int hours) {
        return hours * MILLIS_IN_HOUR;
    }
}
