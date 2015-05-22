package com.simbirsoft.timemeter.ui.model;

import com.simbirsoft.timemeter.model.TaskOverallActivity;

import java.util.Date;

public class DailyTaskActivityDuration {
    public Date date;
    public TaskOverallActivity[] tasks;

    public long getTotalTasksDuration() {
        long totalDuration = 0;
        for (int i = 0; i < tasks.length; i++) {
            totalDuration += tasks[i].getDuration();
        }
        return totalDuration;
    }
}
