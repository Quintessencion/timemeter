package com.simbirsoft.timemeter.db;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.log.LogFactory;

import org.slf4j.Logger;

import java.io.File;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final Logger LOG = LogFactory.getLogger(DatabaseHelper.class);

    private static final String DATABASE_NAME = "timemeter.db";
    private static final int DATABASE_VERSION = 1;

    static {
        cupboard().register(Task.class);
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

        Task task1 = new Task();
        task1.setDescription("Купить продукты");
        task1.setCreateDate(new Date(1389370800000L));

        Task task2 = new Task();
        task2.setDescription("Пропылесосить");
        task2.setCreateDate(new Date(1389729690000L));

        Task task3 = new Task();
        task3.setDescription("Работать на работе");
        task3.setCreateDate(new Date(1389517200000L));

        Task task4 = new Task();
        task4.setDescription("Спать");
        task4.setCreateDate(new Date(1388575800000L));

        cupboard().withDatabase(getWritableDatabase())
                  .put(task1, task2, task3, task4);

        for (int i = 0; i < 45; i++) {
            Task task = new Task();
            task.setDescription("Пробная задача |" + String.valueOf(i) + "|");
            task.setCreateDate(new Date(1419087010000L + i));
            cupboard().withDatabase(getWritableDatabase()).put(task);
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
