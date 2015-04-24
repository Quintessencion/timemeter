package com.simbirsoft.timemeter.jobs;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.be.android.library.worker.base.BaseJob;
import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.models.JobResultStatus;
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.log.LogFactory;

import org.slf4j.Logger;
import javax.inject.Inject;

import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class SaveTagJob extends BaseJob {
    public static final int EVENT_CODE_TAG_ALREADY_EXISTS = 4;
    public static class SaveTagResult extends JobEvent {
        private Tag mTag;

        SaveTagResult(Tag tag, int eventCode) {
            mTag = tag;

            setEventCode(eventCode);
            setJobStatus(JobResultStatus.OK);
        }

        public Tag getTag() {
            return mTag;
        }
    }

    private static final Logger LOG = LogFactory.getLogger(SaveTagJob.class);

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

        Preconditions.checkArgument(mTag != null);
        Preconditions.checkArgument(!TextUtils.isEmpty(mTag.getName()));
    }

    @Override
    protected JobEvent executeImpl() throws Exception {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        DatabaseCompartment cupboard = cupboard().withDatabase(db);

        try {
            db.beginTransaction();
            Tag tag = cupboard.query(Tag.class)
                    .withSelection("UPPER(" + Tag.COLUMN_NAME + ")=?", mTag.getName().toUpperCase())
                    .query()
                    .get();

            if (tag == null) {
                cupboard.put(mTag);
                db.setTransactionSuccessful();
                return new SaveTagResult(mTag, SaveTagResult.EVENT_CODE_OK);
            }
            return new SaveTagResult(mTag, EVENT_CODE_TAG_ALREADY_EXISTS);
        } finally {
            db.endTransaction();
        }
    }
}
