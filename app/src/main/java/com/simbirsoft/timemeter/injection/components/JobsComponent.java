package com.simbirsoft.timemeter.injection.components;

import com.simbirsoft.timemeter.App;
import com.simbirsoft.timemeter.injection.ApplicationModule;
import com.simbirsoft.timemeter.injection.LogicModule;

import dagger.Component;

@Component(modules = { ApplicationModule.class, LogicModule.class })
public interface JobsComponent {
    public App app();
}
