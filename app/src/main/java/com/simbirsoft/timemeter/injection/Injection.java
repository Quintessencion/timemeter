package com.simbirsoft.timemeter.injection;


import com.simbirsoft.timemeter.App;
import com.simbirsoft.timemeter.injection.components.AppComponent;
import com.simbirsoft.timemeter.injection.components.Dagger_AppComponent;
import com.simbirsoft.timemeter.injection.components.DatabaseComponent;
import com.simbirsoft.timemeter.injection.components.JobsComponent;

public final class Injection {
    public static DatabaseComponent sDatabaseComponent;
    public static JobsComponent sJobsComponent;

    public static void init(App appInstance) {
        ApplicationModule appModule = new ApplicationModule(appInstance);

        final AppComponent component = Dagger_AppComponent.builder()
                .applicationModule(appModule)
                .build();

        sJobsComponent = component;
        sDatabaseComponent = component;
    }
}