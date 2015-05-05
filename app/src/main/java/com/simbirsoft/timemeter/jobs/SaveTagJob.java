package com.simbirsoft.timemeter.jobs;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.be.android.library.worker.base.BaseJob;
import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.models.JobResultStatus;
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;

import javax.inject.Inject;

import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class SaveTagJob extends BaseJob {
    public static class SaveTagResult extends JobEvent {
        private Tag mTag;

        SaveTagResult(Tag tag) {
            mTag = tag;

            setJobStatus(JobResultStatus.OK);
        }

        public Tag getTag() {
            return mTag;
        }
    }

    public static final int EVENT_CODE_TAG_ALREADY_EXISTS = 1004;
    public static final int EVENT_CODE_TAG_NAME_IS_EMPTY = 1005;
    private final DatabaseHelper mDatabaseHelper;
    private Tag mTag;

    @Inject
    public SaveTagJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    public void setTag(Tag tag) {
        Preconditions.checkState(mTag == null, "tag already set");
        mTag = tag;
    }

    @Override
    protected void onPreExecute() throws Exception {
        super.onPreExecute();
        Preconditions.checkArgument(mTag != null, "tag is null");
    }

    @Override
    protected JobEvent executeImpl() throws Exception {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        DatabaseCompartment cupboard = cupboard().withDatabase(db);

        try {
            db.beginTransaction();

            if (TextUtils.isEmpty(mTag.getName())) {
                return JobEvent.failure(EVENT_CODE_TAG_NAME_IS_EMPTY, "tag name is empty");
            }

            Tag tag = cupboard.query(Tag.class)
                    .withSelection("UPPER(" + Tag.COLUMN_NAME + ")=?", mTag.getName().toUpperCase())
                    .query()
                    .get();

            if (tag != null) {
                return JobEvent.failure(EVENT_CODE_TAG_ALREADY_EXISTS, "tag already exists");
            }

            cupboard.put(mTag);
            db.setTransactionSuccessful();
            return new SaveTagResult(mTag);
        } finally {
            db.endTransaction();
        }
    }
}
