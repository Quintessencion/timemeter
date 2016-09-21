package com.simbirsoft.timemeter.injection;


import com.simbirsoft.timemeter.App;
import com.simbirsoft.timemeter.injection.components.DaggerInjector;
import com.simbirsoft.timemeter.injection.components.Injector;
import com.simbirsoft.timemeter.injection.components.DatabaseComponent;
import com.simbirsoft.timemeter.injection.components.JobsComponent;
import com.simbirsoft.timemeter.injection.components.TaskManagerComponent;
import com.simbirsoft.timemeter.injection.components.UiComponent;

public final class Injection {
    public static DatabaseComponent sDatabaseComponent;
    public static JobsComponent sJobsComponent;
    public static UiComponent sUiComponent;
    public static TaskManagerComponent sTaskManager;

    public static void init(App appInstance) {
        ApplicationModule appModule = new ApplicationModule(appInstance);

        final Injector component = DaggerInjector.builder()
                .applicationModule(appModule)
                .build();

        sJobsComponent = component;
        sDatabaseComponent = component;
        sUiComponent = component;
        sTaskManager = component;
    }
}
