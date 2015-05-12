package com.simbirsoft.timemeter.injection;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;

import com.simbirsoft.timemeter.App;
import com.simbirsoft.timemeter.controller.ITaskActivityInfoProvider;
import com.simbirsoft.timemeter.controller.ITaskActivityManager;
import com.simbirsoft.timemeter.controller.TaskActivityManager;
import com.simbirsoft.timemeter.controller.TaskNotificationManager;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import android.os.Handler;
import java.util.logging.LogRecord;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    public static final String HANDLER_MAIN = "main";

    private final App mApplication;
    private final Handler mHandler;

    public ApplicationModule(App application) {
        mApplication = application;
        mHandler = new Handler();
    }

    @Provides
    @Named(HANDLER_MAIN)
    Handler provideMainHandler() {
        return mHandler;
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
    Resources provideResources() {
        return mApplication.getResources();
    }

    @Provides
    SQLiteDatabase provideSQLiteDatabase(DatabaseHelper helper) {
        return helper.getWritableDatabase();
    }

    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus(ThreadEnforcer.MAIN);
    }

    @Provides
    @Singleton
    ITaskActivityManager provideTaskActivityManager(Context context, DatabaseHelper helper, Bus bus) {

        TaskActivityManager mgr = new TaskActivityManager(context, bus, helper);
        mgr.initialize();

        return mgr;
    }

    @Provides
    @Singleton
    ITaskActivityInfoProvider provideTaskActivityInfoProvider(ITaskActivityManager taskActivityManager) {
        return taskActivityManager;
    }
}
