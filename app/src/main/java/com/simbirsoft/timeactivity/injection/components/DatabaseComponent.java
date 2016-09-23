package com.simbirsoft.timeactivity.injection.components;

import android.database.sqlite.SQLiteDatabase;

import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.db.Preferences;

public interface DatabaseComponent {
    public DatabaseHelper databaseHelper();
    public SQLiteDatabase database();
    public Preferences preferences();
}
