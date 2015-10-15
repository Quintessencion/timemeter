package com.simbirsoft.timeactivity.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.simbirsoft.timeactivity.db.model.DemoTask;
import com.simbirsoft.timeactivity.db.model.Tag;
import com.simbirsoft.timeactivity.db.model.Task;
import com.simbirsoft.timeactivity.db.model.TaskTag;
import com.simbirsoft.timeactivity.db.model.TaskTimeSpan;
import com.simbirsoft.timeactivity.log.LogFactory;
import com.simbirsoft.timeactivity.ui.util.DatabaseUtils;
import com.squareup.phrase.Phrase;

import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardBuilder;
import nl.qbusict.cupboard.CupboardFactory;
import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

@Singleton
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final Logger LOG = LogFactory.getLogger(DatabaseHelper.class);

    private static final String DATABASE_NAME = "timeactivity.db";
    private static final int DATABASE_VERSION = 2;

    static {
        Cupboard cupboard = new CupboardBuilder().useAnnotations().build();
        CupboardFactory.setCupboard(cupboard);

        cupboard.register(Task.class);
        cupboard.register(Tag.class);
        cupboard.register(TaskTag.class);
        cupboard.register(TaskTimeSpan.class);
        cupboard.register(DemoTask.class);
    }

    @Inject
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static void removeDatabase(Context context) {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        if (dbFile == null || !dbFile.exists()) {
            LOG.debug("unable to delete database: database file not found");
            return;
        }

        boolean isDeleted = dbFile.delete();
        LOG.warn("database file deleted: {}", isDeleted);
    }

    public void initTestData(Context context) {
        removeDatabase(context);

        DatabaseCompartment cupboard = cupboard().withDatabase(getWritableDatabase());
        DatabaseUtils.fillTestData(context, cupboard);
    }

    public void removeTestData() {
        SQLiteDatabase db = getWritableDatabase();
        DatabaseCompartment cupboard = cupboard().withDatabase(db);

        db.beginTransaction();
        try {
            List<DemoTask> demos = cupboard.query(DemoTask.class).query().list();
            String deleteTestSpansStatement = getRemoveTestTimeSpansStatement(demos);
            for (DemoTask task : demos) {
                cupboard.delete(Task.class, task.getId());
            }

            db.delete(DemoTask.TABLE_NAME, null, null);
            cupboard.delete(TaskTimeSpan.class, deleteTestSpansStatement);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private String getRemoveTestTimeSpansStatement(Collection<DemoTask> tasks) {
        final String taskIds = Joiner.on(",").join(Iterables.transform(tasks, DemoTask::getId));

        return Phrase.from("{table_tts}.{tts_task_id} IN ({task_ids})")
                .put("table_tts", TaskTimeSpan.TABLE_NAME)
                .put("tts_task_id", TaskTimeSpan.COLUMN_TASK_ID)
                .put("task_ids", taskIds)
                .format().toString();
    }

    public boolean isDemoDatasExist() {
        SQLiteDatabase db = getWritableDatabase();
        DatabaseCompartment cupboard = cupboard().withDatabase(db);

        boolean result = false;
        Cursor c = cupboard.query(DemoTask.class).getCursor();
        try {
            c.moveToFirst();
            result = c.getCount() > 0;
        } finally {
            c.close();
        }

        return result;
    }

    public static void backupDatabase(Context context) {
        try {
            File externalStorage = Environment.getExternalStorageDirectory();
            if (externalStorage.canWrite()) {
                File currentDB = context.getDatabasePath(DATABASE_NAME);
                File backupDB = new File(externalStorage, DATABASE_NAME);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    LOG.debug("backed up database to '{}'", backupDB);
                } else {
                    LOG.error("unable to backup database: database is not exists");
                }

            } else {
                LOG.error("unable to backup database: external storage is not writable");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        cupboard().withDatabase(db).createTables();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This will upgrade tables, adding columns and new tables.
        // Note that existing columns will not be converted
        cupboard().withDatabase(db).upgradeTables();
        // do migration work
    }
}
