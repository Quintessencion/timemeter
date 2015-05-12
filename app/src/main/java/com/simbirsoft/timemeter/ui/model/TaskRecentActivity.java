package com.simbirsoft.timemeter.ui.model;

import android.content.res.Resources;

import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class TaskRecentActivity {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yy");

    private List<TaskActivityItem> mList;
    private final Calendar mRecentActivityTime;

    public TaskRecentActivity() {
        mRecentActivityTime = Calendar.getInstance();
    }

    public List<TaskActivityItem> getList() {
        return (mList != null) ? mList : Lists.newArrayList();
    }

    public void setList(List<TaskActivityItem> list) {
        mList = list;
    }

    public long getRecentActivityTime() {
        return mRecentActivityTime.getTimeInMillis();
    }

    public void setRecentActivityTime(long recentActivityTime) {
            mRecentActivityTime.setTimeInMillis(recentActivityTime);
    }

    public String getEmptyIndicatorMessage(Resources res) {
        if (mRecentActivityTime.getTimeInMillis() == 0) {
            return res.getString(R.string.no_activity);
        }
        StringBuilder builder = new StringBuilder(res.getString(R.string.recent_activity).toLowerCase());
        builder.append(" ").append(DATE_FORMAT.format(mRecentActivityTime.getTime()));
        return builder.toString();
    }
}
