package com.simbirsoft.timemeter;

import android.app.Application;

import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.log.LogFactory;

import org.slf4j.Logger;

public class App extends Application {

    private static final Logger LOG = LogFactory.getLogger(App.class);

    @Override
    public void onCreate() {
        super.onCreate();

        Injection.init(this);

        if (BuildConfig.DEBUG) {
//            DatabaseHelper.removeDatabase(this);
        }

        LOG.info("App created");
    }
}
