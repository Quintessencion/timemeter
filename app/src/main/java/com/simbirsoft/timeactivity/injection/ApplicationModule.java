package com.simbirsoft.timeactivity.injection;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;

import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.controllers.WorkerJobManager;
import com.simbirsoft.timeactivity.App;
import com.simbirsoft.timeactivity.Consts;
import com.simbirsoft.timeactivity.controller.ITaskActivityInfoProvider;
import com.simbirsoft.timeactivity.controller.ITaskActivityManager;
import com.simbirsoft.timeactivity.controller.TaskActivityManager;
import com.simbirsoft.timeactivity.db.DatabaseHelper;
import com.simbirsoft.timeactivity.service.TimeWorkerService;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import android.os.Handler;
import java.text.DateFormat;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    public static final String HANDLER_MAIN = "main";
    public static final String DATE_FORMAT = "date_format";

    private final App mApplication;
    private final Handler mHandler;
    private final JobManager mJobManager;

    public ApplicationModule(App application) {
        mApplication = application;
        mHandler = new Handler();

        mJobManager = new WorkerJobManager(
                application,
                Consts.WORKER_THREAD_POOL_COUNT,
                TimeWorkerService.class);

        JobManager.init(mJobManager);
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
    JobManager provideJobManager() {
        return mJobManager;
    }

    @Provides
    @Named(DATE_FORMAT)
    DateFormat provideDateFormat() {
        return android.text.format.DateFormat.getDateFormat(mApplication);
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
    ITaskActivityManager provideTaskActivityManager(JobManager jobManager, Context context, DatabaseHelper helper, Bus bus) {
        TaskActivityManager mgr = new TaskActivityManager(jobManager, context, bus, helper);
        mgr.initialize();

        return mgr;
    }

    @Provides
    @Singleton
    ITaskActivityInfoProvider provideTaskActivityInfoProvider(ITaskActivityManager taskActivityManager) {
        return taskActivityManager;
    }
}
