package com.simbirsoft.timemeter.jobs;

import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.persist.XmlTag;

import java.util.List;

import javax.inject.Inject;

import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class SaveBackupTagsJob extends LoadJob{

    private List<XmlTag> tags;
    private DatabaseHelper databaseHelper;

    @Inject
    public SaveBackupTagsJob(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    @Override
    protected LoadJobResult<?> performLoad() throws Exception {
        List<Tag> dbTags = Injection.sJobsComponent.loadTagListJob().getTagList();
        List<Tag> actualTags = Lists.newArrayList();

        for (XmlTag xmlTag: tags) {
            Tag tag = xmlTag.getTag();
            if (!checkForExist(dbTags, tag)) {
                actualTags.add(tag);
            }
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        DatabaseCompartment cupboard = cupboard().withDatabase(db);

        try {
            db.beginTransaction();

            cupboard.put(actualTags);
            db.setTransactionSuccessful();
            return LoadJobResult.loadOk();
        }
        finally {
            db.endTransaction();
        }
    }

    public void setTags(List<XmlTag> tags) {
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