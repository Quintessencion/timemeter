package com.simbirsoft.timeactivity;

import android.app.Application;

import com.simbirsoft.timeactivity.injection.Injection;
import com.simbirsoft.timeactivity.log.LogFactory;

import org.slf4j.Logger;

public class App extends Application {

    private static final Logger LOG = LogFactory.getLogger(App.class);

    @Override
    public void onCreate() {
        super.onCreate();

        Injection.init(this);

        if (!Injection.sDatabaseComponent.preferences().isDatabaseTestDataInitialized()) {
            Injection.sDatabaseComponent.databaseHelper().initTestData(this);
            Injection.sDatabaseComponent.preferences().setDatabaseTestDataInitialized(true);
        }

        LOG.info("App created");
    }
}
