package com.simbirsoft.timemeter.jobs;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.be.android.library.worker.base.BaseJob;
import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.models.JobResultStatus;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTag;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.model.TaskBundle;

import org.slf4j.Logger;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class SaveTagJob extends BaseJob {
    public static class SaveTagResult extends JobEvent {

        private Tag mTag;

        SaveTagResult(Tag tag) {
            mTag = tag;

            setEventCode(EVENT_CODE_OK);
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

        if (mTag.hasId()) {
            LOG.trace("saving tag {}", mTag);
            cupboard.put(mTag);
            LOG.trace("saved tag {}", mTag);

            return new SaveTagResult(mTag);
        } else {
            try {
                db.beginTransaction();

                Tag tag = cupboard.query(Tag.class)
                        .withSelection(Tag.COLUMN_NAME + "=?", mTag.getName())
                        .query()
                        .get();

                Preconditions.checkState((tag == null), String.format("tag name:'%s' have already exists", mTag.getName()));

                LOG.trace("saving tag {}", mTag);
                cupboard.put(mTag);
                LOG.trace("saved tag {}", mTag);

                db.setTransactionSuccessful();

                return new SaveTagResult(mTag);
            } finally {
                db.endTransaction();
            }
        }
    }
}
