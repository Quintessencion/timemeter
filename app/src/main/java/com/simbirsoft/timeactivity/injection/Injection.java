package com.simbirsoft.timeactivity.injection;


import com.simbirsoft.timeactivity.App;
import com.simbirsoft.timeactivity.injection.components.DatabaseComponent;
import com.simbirsoft.timeactivity.injection.components.JobsComponent;
import com.simbirsoft.timeactivity.injection.components.TaskManagerComponent;
import com.simbirsoft.timeactivity.injection.components.UiComponent;

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
