package com.simbirsoft.timemeter.injection;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.simbirsoft.timemeter.App;
import com.simbirsoft.timemeter.controller.ITaskActivityManager;
import com.simbirsoft.timemeter.controller.TaskActivityManager;
import com.simbirsoft.timemeter.db.DatabaseHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    private App mApplication;

    public ApplicationModule(App application) {
        this.mApplication = application;
    }

    @Provides
    App provideApplication() {
        return mApplication;
    }

    @Provides
    Context provideContext() {
        return mApplication;
    }

    @Provides
    SQLiteDatabase provideSQLiteDatabase(DatabaseHelper helper) {
        return helper.getWritableDatabase();
    }

    @Provides
    @Singleton
    ITaskActivityManager provideTaskActivityManager(Context context, DatabaseHelper helper) {
        TaskActivityManager mgr = new TaskActivityManager(context, helper);
        mgr.initialize();

        return mgr;
    }
}
