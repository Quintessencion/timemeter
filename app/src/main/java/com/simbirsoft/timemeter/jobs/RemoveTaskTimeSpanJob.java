package com.simbirsoft.timemeter.jobs;

import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.base.BaseJob;
import com.be.android.library.worker.base.JobEvent;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;

import java.util.Collection;

import javax.inject.Inject;

import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class RemoveTaskTimeSpanJob extends BaseJob {
    private DatabaseHelper mDatabaseHelper;
    private Collection<Long> mSpanIds;

    @Inject
    public RemoveTaskTimeSpanJob(DatabaseHelper dbHelper) {
        mDatabaseHelper = dbHelper;
    }

    public void setSpan(Collection<Long> spanIds) {
        mSpanIds = spanIds;
    }

    @Override
    protected JobEvent executeImpl() throws Exception {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        DatabaseCompartment cb = cupboard().withDatabase(db);

        try {
            db.beginTransaction();
            for (long id : mSpanIds) {
                cb.delete(TaskTimeSpan.class, id);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return JobEvent.ok();
    }
}
