package com.simbirsoft.timemeter.model;

import java.security.InvalidParameterException;
import java.util.Calendar;

public enum Period {
    DAY,
    WEEK,
    MONTH,
    YEAR,
    ALL,
    OTHER;

    public static long getPeriodEnd(Period period, long startTimeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTimeMillis);

        switch (period) {
            case DAY:
                calendar.add(Calendar.DATE, 1);

                return calendar.getTimeInMillis();

            case WEEK:
                calendar.add(Calendar.WEEK_OF_YEAR, 1);

                return calendar.getTimeInMillis();

            case MONTH:
                calendar.add(Calendar.MONTH, 1);

                return calendar.getTimeInMillis();

            case YEAR:
                calendar.add(Calendar.YEAR, 1);

                return calendar.getTimeInMillis();

            case OTHER:
                throw new InvalidParameterException("Cannot be used with parameter OTHER");
        }

        return 0;
    }
}
