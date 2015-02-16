package com.simbirsoft.timemeter.util;

import android.support.annotation.Nullable;

import com.simbirsoft.timemeter.model.Period;

public final class SqlUtils {

    /**
     * @param dateMillis period beginning
     * @param period optional period
     * @return SQL period selection statement or empty string
     * if <code>dateMillis</code> is <code>0</code>.
     */
    public static String periodToStatement(String beginTimeColumnName, long dateMillis, @Nullable Period period) {
        String where = "";

        if (dateMillis == 0) {
            return where;
        }

        long periodEnd = 0;
        if (period != null) {
            periodEnd = Period.getPeriodEnd(period, dateMillis);
        }

        where += beginTimeColumnName + " >= " + String.valueOf(dateMillis);

        if (periodEnd > 0) {
            where += " AND " + beginTimeColumnName + " < " + String.valueOf(periodEnd);
        }

        return where;
    }

}
