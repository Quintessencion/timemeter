package com.simbirsoft.timemeter.controller;

import com.simbirsoft.timemeter.db.Preferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HelpCardController {

    Preferences mPreferences;

    @Inject
    public HelpCardController(Preferences preferences) {
        mPreferences = preferences;
    }
}
