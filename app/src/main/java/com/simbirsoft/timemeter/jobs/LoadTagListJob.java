package com.simbirsoft.timemeter.jobs;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.JobResultStatus;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;

import java.util.List;

import javax.inject.Inject;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LoadTagListJob extends LoadJob {

    private final DatabaseHelper mDatabaseHelper;

    @Inject
    public LoadTagListJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    @Override
    protected LoadJobResult<?> performLoad() {
        List<Tag> tags = cupboard()
                .withDatabase(mDatabaseHelper.getWritableDatabase())
                .query(Tag.class)
                .list();

        return new LoadJobResult<>(JobResultStatus.OK, tags);
    }
}
