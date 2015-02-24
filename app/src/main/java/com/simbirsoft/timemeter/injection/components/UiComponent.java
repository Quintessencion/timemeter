package com.simbirsoft.timemeter.injection.components;

import android.content.Context;

import com.simbirsoft.timemeter.receiver.NotificationUpdateReceiver;
import com.simbirsoft.timemeter.receiver.ScreenLockReceiver;
import com.simbirsoft.timemeter.receiver.StopTaskActivityReceiver;
import com.simbirsoft.timemeter.ui.main.MainPagerFragment;
import com.simbirsoft.timemeter.ui.stats.StatsListFragment;
import com.simbirsoft.timemeter.ui.tasklist.TaskListFragment;
import com.simbirsoft.timemeter.ui.views.FilterView;

public interface UiComponent {
    public Context context();
    public void injectNotificationUpdateReceiver(NotificationUpdateReceiver receiver);
    public void injectScreenLockReceiver(ScreenLockReceiver receiver);
    public void injectStopTaskActivityReceiver(StopTaskActivityReceiver receiver);
    public void injectTaskListFragment(TaskListFragment fragment);
    public void injectFilterView(FilterView filterView);
    public void injectStatsListFragment(StatsListFragment fragment);
    public void injectMainPagerFragment(MainPagerFragment fragment);
}
