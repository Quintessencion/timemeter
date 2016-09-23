package com.simbirsoft.timeactivity.injection;

import com.simbirsoft.timeactivity.injection.components.DatabaseComponent;
import com.simbirsoft.timeactivity.injection.components.JobsComponent;
import com.simbirsoft.timeactivity.injection.components.TaskManagerComponent;
import com.simbirsoft.timeactivity.injection.components.UiComponent;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface Injector extends JobsComponent,
        DatabaseComponent, UiComponent, TaskManagerComponent {
}
