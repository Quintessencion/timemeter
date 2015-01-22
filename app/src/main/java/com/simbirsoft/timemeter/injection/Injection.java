package com.simbirsoft.timemeter.injection;


import com.simbirsoft.timemeter.App;
import com.simbirsoft.timemeter.injection.components.Dagger_DatabaseComponent;
import com.simbirsoft.timemeter.injection.components.Dagger_JobsComponent;
import com.simbirsoft.timemeter.injection.components.DatabaseComponent;
import com.simbirsoft.timemeter.injection.components.JobsComponent;

public final class Injection {
    public static JobsComponent sJobsComponent;
    public static DatabaseComponent sDatabaseComponent;

    public static void init(App appInstance) {
        ApplicationModule appModule = new ApplicationModule(appInstance);
        LogicModule logicModule = new LogicModule();

        sJobsComponent = Dagger_JobsComponent.builder()
                .applicationModule(appModule)
                .logicModule(logicModule)
                .build();

        sDatabaseComponent = Dagger_DatabaseComponent.builder()
                .applicationModule(appModule)
                .databaseModule(new DatabaseModule())
                .build();
    }
}
