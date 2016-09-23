package com.simbirsoft.timeactivity.jobs;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;

import com.be.android.library.worker.base.BaseJob;
import com.be.android.library.worker.base.JobEvent;
import com.google.common.collect.Lists;
import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.db.model.TaskTimeSpan;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class RestoreTaskTimeSpansJob extends BaseJob {
    private DatabaseHelper mDatabaseHelper;
    private Bundle mBackupBundle;

    @Inject
    public RestoreTaskTimeSpansJob(DatabaseHelper dbHelper) {
        mDatabaseHelper = dbHelper;
    }

    public void setBackupBundle(Bundle backupBundle) {
        mBackupBundle = backupBundle;
    }

    @Override
    protected JobEvent executeImpl() throws Exception {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        DatabaseCompartment cb = cupboard().withDatabase(db);

        try {
            db.beginTransaction();
            ArrayList<Parcelable> ps = mBackupBundle.getParcelableArrayList("removed_spans");
            List<TaskTimeSpan> spans = Lists.transform(ps, item -> (TaskTimeSpan)item);
            cb.put(spans);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return JobEvent.ok();
    }
}
