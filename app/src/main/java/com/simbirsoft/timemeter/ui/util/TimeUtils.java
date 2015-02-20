package com.simbirsoft.timemeter.ui.util;

import java.util.Calendar;

public final class TimeUtils {

    public static long getDayStartMillis(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTimeInMillis();
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

}
