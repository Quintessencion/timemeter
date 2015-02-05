package com.simbirsoft.timemeter.jobs;

import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.base.BaseJob;
import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.TaskTag;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.model.TagBundle;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.squareup.phrase.Phrase;

import org.slf4j.Logger;

import java.util.List;

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
