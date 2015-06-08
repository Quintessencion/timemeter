package com.simbirsoft.timemeter.util;

import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref
public interface SharedPreferences {

    // default start time is 8:00 am in minutes
    @DefaultInt(8 * 60)
    int calendarStartTime();

    // default start time is 9:00 pm in minutes
    @DefaultInt(21 * 60)
    int calendarEndTime();
}
