package com.simbirsoft.timeactivity.ui.model;

import android.content.res.Resources;

import com.google.common.collect.Lists;
import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.injection.ApplicationModule;
import com.simbirsoft.timeactivity.injection.Injection;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class TaskRecentActivity {

    @Inject
    @Named(ApplicationModule.DATE_FORMAT)
    DateFormat mDateFormat;

    private List<TaskActivityItem> mList;
    private final Calendar mRecentActivityTime;

    public TaskRecentActivity() {
        mRecentActivityTime = Calendar.getInstance();
        Injection.sUiComponent.injectTaskRecentActivity(this);
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
        builder.append(" ").append(mDateFormat.format(mRecentActivityTime.getTime()));
        return builder.toString();
    }
}
