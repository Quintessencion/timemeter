package com.simbirsoft.timemeter.injection.components;

import android.content.Context;

import com.simbirsoft.timemeter.receiver.NotificationUpdateReceiver;
import com.simbirsoft.timemeter.receiver.ScreenLockReceiver;
import com.simbirsoft.timemeter.receiver.StopTaskActivityReceiver;
import com.simbirsoft.timemeter.ui.calendar.ActivityCalendarFragment;
import com.simbirsoft.timemeter.ui.main.MainPageFragment;
import com.simbirsoft.timemeter.ui.main.MainPagerFragment;
import com.simbirsoft.timemeter.ui.model.TaskRecentActivity;
import com.simbirsoft.timemeter.ui.stats.StatisticsViewBinder;
import com.simbirsoft.timemeter.ui.stats.StatsDetailsFragment;
import com.simbirsoft.timemeter.ui.stats.StatsListFragment;
import com.simbirsoft.timemeter.ui.activities.TaskActivitiesFragment;
import com.simbirsoft.timemeter.ui.stats.binders.ActivityStackedTimelineBinder;
import com.simbirsoft.timemeter.ui.stats.binders.ActivityTimelineBinder;
import com.simbirsoft.timemeter.ui.stats.binders.OverallActivityTimePieBinder;
import com.simbirsoft.timemeter.ui.tags.EditTagNameDialogFragment;
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
    public void injectStatsDetailsFragment(StatsDetailsFragment fragment);
    public void injectActivityCalendarFragment(ActivityCalendarFragment fragment);
    public void injectTaskActivitiesFragment(TaskActivitiesFragment fragment);
    public void injectOverallActivityTimePieBinder(OverallActivityTimePieBinder binder);
    public void injectActivityTimelineBinder(ActivityTimelineBinder activityTimelineBinder);
    public void injectActivityStackedTimelineBinder(ActivityStackedTimelineBinder activityStackedTimelineBinder);
    public void injectEditTagNameDialogFragment(EditTagNameDialogFragment editTagNameDialogFragment);
    public void injectMainPageFragment(MainPageFragment fragment);
    public void injectTaskRecentActivity(TaskRecentActivity activity);
}
