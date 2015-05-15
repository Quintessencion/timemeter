package com.simbirsoft.timemeter.jobs;

import android.database.Cursor;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.squareup.phrase.Phrase;

import java.util.List;

import javax.inject.Inject;


public class LoadTagNamesJob extends LoadJob {

    @Inject
    public LoadTagNamesJob(DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    private final DatabaseHelper mDatabaseHelper;

    @Override
    protected LoadJobResult<List<String>> performLoad() {
        final String query = Phrase.from("SELECT {table_tag}.{table_tag__name} FROM {table_tag}")
                .put("table_tag", Tag.TABLE_NAME)
                .put("table_tag__name", Tag.COLUMN_NAME)
                .format()
                .toString();
        final Cursor c = mDatabaseHelper.getReadableDatabase().rawQuery(query, null);
        final List<String> result = Lists.newArrayListWithCapacity(c.getCount());
        try {
            while (c.moveToNext()) {
                result.add(c.getString(0));
            }
            return new LoadJobResult<>(result);
        } finally {
            c.close();
        }
    }
}
