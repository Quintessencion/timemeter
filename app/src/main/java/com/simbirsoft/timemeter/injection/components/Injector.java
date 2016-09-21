package com.simbirsoft.timemeter.injection.components;

import com.simbirsoft.timemeter.injection.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface Injector extends JobsComponent,
        DatabaseComponent, UiComponent, TaskManagerComponent {
}
