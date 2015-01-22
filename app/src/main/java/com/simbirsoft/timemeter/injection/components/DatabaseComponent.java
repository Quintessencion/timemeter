package com.simbirsoft.timemeter.injection.components;

import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.injection.ApplicationModule;
import com.simbirsoft.timemeter.injection.DatabaseModule;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {ApplicationModule.class, DatabaseModule.class})
@Singleton
public interface DatabaseComponent {
    public DatabaseHelper databaseHelper();
}
