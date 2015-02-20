package com.simbirsoft.timemeter.db;

import android.database.sqlite.SQLiteDatabase;

import com.simbirsoft.timemeter.db.model.TaskTimeSpan;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public final class QueryHelper {

    public static long findFirstActivityBeginDate(SQLiteDatabase db) {
        TaskTimeSpan min = cupboard().withDatabase(db)
                .query(TaskTimeSpan.class)
                .having("MIN(" + TaskTimeSpan.COLUMN_START_TIME + ")")
                .groupBy(TaskTimeSpan.COLUMN_ID)
                .get();

        if (min == null) {
            return 0;
        }

        return min.getStartTimeMillis();
    }
}
