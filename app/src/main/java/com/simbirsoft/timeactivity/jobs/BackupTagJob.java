package com.simbirsoft.timeactivity.jobs;

import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.base.BaseJob;
import com.be.android.library.worker.base.JobEvent;
import com.google.common.base.Preconditions;
import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.db.model.Tag;
import com.simbirsoft.timeactivity.log.LogFactory;
import com.simbirsoft.timeactivity.ui.model.TagBundle;

import org.slf4j.Logger;

import javax.inject.Inject;

import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class BackupTagJob extends BaseJob {

    private static final Logger LOG = LogFactory.getLogger(BackupTagJob.class);

    private final DatabaseHelper mDatabaseHelper;
    private TagBundle mTagBundle;

    @Inject
    public BackupTagJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    @Override
    protected void onPreExecute() throws Exception {
        super.onPreExecute();

        Preconditions.checkArgument(mTagBundle != null);
    }

    public void setTagBundle(TagBundle tagBundle) {
        Preconditions.checkArgument(mTagBundle == null);

        mTagBundle = tagBundle;
    }

    @Override
    protected JobEvent executeImpl() throws Exception {
        final Tag tag = mTagBundle.getTag();
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        DatabaseCompartment cupboard = cupboard().withDatabase(db);

        LOG.trace("backup tag id:'{}'..", tag.getId());
        try {
            db.beginTransaction();

            cupboard.put(tag);
            cupboard.put(mTagBundle.getTaskTags());

            db.setTransactionSuccessful();

            LOG.trace("tag id:'{}' backup finished", tag.getId());

            return JobEvent.ok();

        } finally {
            db.endTransaction();
        }
    }
}
