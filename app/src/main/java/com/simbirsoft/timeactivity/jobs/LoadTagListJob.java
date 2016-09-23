package com.simbirsoft.timeactivity.jobs;

import android.database.Cursor;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.JobResultStatus;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Joiner;
import com.squareup.phrase.Phrase;
import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.db.model.Tag;

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
        List<Tag> tags = getTagList();
        return new LoadJobResult<>(JobResultStatus.OK, tags);
    }

    public List<Tag> getTagList() {
        return cupboard()
                .withDatabase(mDatabaseHelper.getWritableDatabase())
                .query(Tag.class)
                .orderBy(Tag.COLUMN_NAME)
                .list();
    }

    public List<Tag> getTagListWereIds(List<Long> ids) {
        final String coll_id = Joiner.on(",").join(ids);

        final String query = Phrase.from("SELECT {table_name}.* FROM {table_name} WHERE {table_name}.{column_id} IN ({coll_id})")
                .put("table_name", Tag.TABLE_NAME)
                .put("column_id", Tag.COLUMN_ID)
                .put("coll_id", coll_id)
                .format().toString();

        final Cursor cursor = mDatabaseHelper.getReadableDatabase().rawQuery(query, null);

        try {
            return cupboard().withCursor(cursor).list(Tag.class);
        }
        finally {
            cursor.close();
        }
    }
}
