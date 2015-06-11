package com.simbirsoft.timemeter.jobs;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;

import com.be.android.library.worker.base.BaseJob;
import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;

import java.util.ArrayList;
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
    protected LoadJobResult<Bundle> executeImpl() throws Exception {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        DatabaseCompartment cb = cupboard().withDatabase(db);

        Bundle removedSpans = new Bundle();
        try {
            db.beginTransaction();
            ArrayList<TaskTimeSpan> ps = new ArrayList<>();
            for (long id : mSpanIds) {
                ps.add(cb.get(TaskTimeSpan.class, id));
                cb.delete(TaskTimeSpan.class, id);
            }
            removedSpans.putParcelableArrayList("removed_spans", ps);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return new LoadJobResult<>(removedSpans);
    }
}
