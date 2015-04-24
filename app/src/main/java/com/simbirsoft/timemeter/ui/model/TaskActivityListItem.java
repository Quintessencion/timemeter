package com.simbirsoft.timemeter.ui.model;


import com.simbirsoft.timemeter.db.model.TaskTimeSpan;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

public class TaskActivityListItem extends TaskActivityItem {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d");
    private static final SimpleDateFormat WEEK_DAY_FORMAT = new SimpleDateFormat("EE");

    private List<TaskTimeSpan> mList;

    public TaskActivityListItem() {
        super();
    }

    public List<TaskTimeSpan> getList() {
        return (mList == null) ? Collections.<TaskTimeSpan>emptyList() : mList;
    }

    public void setList(List<TaskTimeSpan> list) {
        mList = list;
    }

    public String getDateString() {
        return DATE_FORMAT.format(getDate());
    }

    public String getWeekDayString() {
        return WEEK_DAY_FORMAT.format(getDate());
    }
}
