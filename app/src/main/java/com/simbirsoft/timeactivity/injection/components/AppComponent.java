package com.simbirsoft.timeactivity.injection.components;

import com.simbirsoft.timeactivity.injection.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface AppComponent extends JobsComponent,
        DatabaseComponent, UiComponent, TaskManagerComponent {
}
