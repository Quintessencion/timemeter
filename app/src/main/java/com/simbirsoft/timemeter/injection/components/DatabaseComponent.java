package com.simbirsoft.timemeter.injection.components;

import android.database.sqlite.SQLiteDatabase;

import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.Preferences;
import com.simbirsoft.timemeter.injection.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

public interface DatabaseComponent {
    public DatabaseHelper databaseHelper();
    public SQLiteDatabase database();
    public Preferences preferences();
}
