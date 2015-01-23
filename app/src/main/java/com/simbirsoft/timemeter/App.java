package com.simbirsoft.timemeter;

import android.app.Application;

import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.controllers.WorkerJobManager;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.service.TimeWorkerService;

import org.slf4j.Logger;

public class App extends Application {

    private static final Logger LOG = LogFactory.getLogger(App.class);

    @Override
    public void onCreate() {
        super.onCreate();

        Injection.init(this);

        if (BuildConfig.DEBUG) {
//            DatabaseHelper.removeDatabase(this);
            Injection.sDatabaseComponent.databaseHelper().initTestData(this);
        }

        JobManager.init(new WorkerJobManager(
                this,
                Consts.WORKER_THREAD_POOL_COUNT,
                TimeWorkerService.class));

        LOG.info("App created");
    }
}
