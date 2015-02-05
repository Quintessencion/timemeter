package com.simbirsoft.timemeter.jobs;

import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.base.BaseJob;
import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTag;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.model.TagBundle;
import com.squareup.phrase.Phrase;

import org.slf4j.Logger;

import java.util.List;

import javax.inject.Inject;

import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class RemoveTagJob extends BaseJob {

    private static final Logger LOG = LogFactory.getLogger(RemoveTagJob.class);

    private final DatabaseHelper mDatabaseHelper;
    private Long mTagId;

    @Inject
    public RemoveTagJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    @Override
    protected void onPreExecute() throws Exception {
        super.onPreExecute();

        Preconditions.checkArgument(mTagId != null);
    }

    public void setTagId(long tagId) {
        Preconditions.checkArgument(mTagId == null);

        mTagId = tagId;
    }

    @Override
    protected JobEvent executeImpl() throws Exception {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        DatabaseCompartment cupboard = cupboard().withDatabase(db);

        LOG.trace("removing tag id:'{}'", mTagId);
        final String selection =
                Phrase.from("{tag_id}=?")
                        .put("tag_id", TaskTag.COLUMN_TAG_ID)
                        .format()
                        .toString();
        try {
            db.beginTransaction();

            List<TaskTag> tags = cupboard.query(TaskTag.class)
                    .withSelection(selection, String.valueOf(mTagId))
                    .query()
                    .list();
            Tag tag = cupboard.query(Tag.class)
                    .withSelection(Tag.COLUMN_ID + "=?", String.valueOf(mTagId))
                    .query()
                    .get();
            Preconditions.checkNotNull(tag, String.format("tag id:'%d' not found", mTagId));
            TagBundle tagBundle = TagBundle.create(tag, tags);

            int count = cupboard.delete(
                    TaskTag.class,
                    Phrase.from("{tag_id}=?")
                            .put("tag_id", TaskTag.COLUMN_TAG_ID)
                            .format()
                            .toString(),
                    String.valueOf(mTagId));

            LOG.trace("'{}' task tags removed for tag id:'{}'", count, mTagId);

            cupboard.delete(Tag.class, mTagId);
            LOG.trace("tag id:'{}' removed", mTagId);

            db.setTransactionSuccessful();

            return new LoadJobResult<>(tagBundle);

        } finally {
            db.endTransaction();
        }
    }
}
