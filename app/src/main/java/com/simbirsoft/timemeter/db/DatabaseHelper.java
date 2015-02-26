package com.simbirsoft.timemeter.db;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.common.io.Closeables;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTag;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.persist.XmlTag;
import com.simbirsoft.timemeter.persist.XmlTagRef;
import com.simbirsoft.timemeter.persist.XmlTask;
import com.simbirsoft.timemeter.persist.XmlTaskList;
import com.simbirsoft.timemeter.persist.XmlTaskListReader;

import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardBuilder;
import nl.qbusict.cupboard.CupboardFactory;
import nl.qbusict.cupboard.DatabaseCompartment;

@Singleton
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final Logger LOG = LogFactory.getLogger(DatabaseHelper.class);

    private static final String DATABASE_NAME = "timemeter.db";
    private static final int DATABASE_VERSION = 1;

    static {
        Cupboard cupboard = new CupboardBuilder().useAnnotations().build();
        CupboardFactory.setCupboard(cupboard);

        cupboard.register(Task.class);
        cupboard.register(Tag.class);
        cupboard.register(TaskTag.class);
        cupboard.register(TaskTimeSpan.class);
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

        InputStream in = null;
        try {
            in = context.getAssets().open("testdata/tasklist-ru.xml");
            XmlTaskList taskList = XmlTaskListReader.readXml(in);
            LOG.trace("task list read successfully");

            for (XmlTag xmlTag : taskList.getTagList()) {
                Tag tag = xmlTag.getTag();
                cupboard.put(tag);
            }

            for (XmlTask xmlTask : taskList.getTaskList()) {
                Task task = xmlTask.getTask();
                cupboard.put(task);
                xmlTask.setId(task.getId());
                List<TaskTimeSpan> spans = xmlTask.getTaskActivity();
                cupboard.put(spans);

                for (XmlTagRef tagRef : xmlTask.getTagList()) {
                    TaskTag taskTag = new TaskTag();
                    taskTag.setTaskId(task.getId());
                    taskTag.setTagId(tagRef.getTagId());
                    cupboard.put(taskTag);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            Closeables.closeQuietly(in);
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
