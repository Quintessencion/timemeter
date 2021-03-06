package com.simbirsoft.timeactivity.jobs;

import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.db.model.Tag;
import com.simbirsoft.timeactivity.injection.Injection;

import java.util.List;

import javax.inject.Inject;

import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class SaveBackupTagsJob extends LoadJob{

    private List<Tag> tags;
    private DatabaseHelper databaseHelper;

    @Inject
    public SaveBackupTagsJob(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    @Override
    protected LoadJobResult<?> performLoad() throws Exception {
        List<Tag> dbTags = Injection.sJobsComponent.loadTagListJob().getTagList();
        List<Tag> actualTags = Lists.newArrayList(Collections2.filter(tags, input -> !checkForExist(dbTags, input)));

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        DatabaseCompartment cupboard = cupboard().withDatabase(db);

        try {
            db.beginTransaction();

            cupboard.put(actualTags);
            db.setTransactionSuccessful();
            return new LoadJobResult<>(actualTags);
        }
        finally {
            db.endTransaction();
        }
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    private boolean checkForExist(List<Tag> dbTags, Tag tag) {
        for (Tag dbTag: dbTags) {
            if (dbTag.getName().equals(tag.getName())) {
                return true;
            }
        }
        return false;
    }
}