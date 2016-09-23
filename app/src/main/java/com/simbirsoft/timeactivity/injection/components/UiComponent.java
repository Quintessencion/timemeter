package com.simbirsoft.timeactivity.injection.components;

import android.content.Context;

import com.simbirsoft.timeactivity.receiver.NotificationUpdateReceiver;
import com.simbirsoft.timeactivity.receiver.ScreenLockReceiver;
import com.simbirsoft.timeactivity.receiver.StopTaskActivityReceiver;
import com.simbirsoft.timeactivity.ui.calendar.ActivityCalendarFragment;
import com.simbirsoft.timeactivity.ui.main.MainPageFragment;
import com.simbirsoft.timeactivity.ui.main.MainPagerFragment;
import com.simbirsoft.timeactivity.ui.main.NavigationDrawerFragment;
import com.simbirsoft.timeactivity.ui.model.TaskRecentActivity;
import com.simbirsoft.timeactivity.ui.stats.StatsDetailsFragment;
import com.simbirsoft.timeactivity.ui.stats.StatsListFragment;
import com.simbirsoft.timeactivity.ui.activities.TaskActivitiesFragment;
import com.simbirsoft.timeactivity.ui.stats.binders.ActivityStackedTimelineBinder;
import com.simbirsoft.timeactivity.ui.stats.binders.ActivityTimelineBinder;
import com.simbirsoft.timeactivity.ui.stats.binders.OverallActivityTimePieBinder;
import com.simbirsoft.timeactivity.ui.tags.EditTagNameDialogFragment;
import com.simbirsoft.timeactivity.ui.tags.TagListFragment;
import com.simbirsoft.timeactivity.ui.taskedit.ViewTaskFragment;
import com.simbirsoft.timeactivity.ui.tasklist.TaskListFragment;
import com.simbirsoft.timeactivity.ui.views.FilterView;
import com.simbirsoft.timeactivity.ui.activities.EditTaskActivityDialogFragment;
import com.simbirsoft.timeactivity.ui.settings.ImportStatsDialog;
import com.simbirsoft.timeactivity.ui.settings.SettingsFragment;

public interface UiComponent {
    public Context context();
    public void injectNotificationUpdateReceiver(NotificationUpdateReceiver receiver);
    public void injectScreenLockReceiver(ScreenLockReceiver receiver);
    public void injectStopTaskActivityReceiver(StopTaskActivityReceiver receiver);
    public void injectTaskListFragment(TaskListFragment fragment);
    public void injectFilterView(FilterView filterView);
    public void injectStatsListFragment(StatsListFragment fragment);
    public void injectMainPagerFragment(MainPagerFragment fragment);
    public void injectNavigationDrawerFragment(NavigationDrawerFragment fragment);
    public void injectStatsDetailsFragment(StatsDetailsFragment fragment);
    public void injectActivityCalendarFragment(ActivityCalendarFragment fragment);
    public void injectTaskActivitiesFragment(TaskActivitiesFragment fragment);
    public void injectOverallActivityTimePieBinder(OverallActivityTimePieBinder binder);
    public void injectActivityTimelineBinder(ActivityTimelineBinder activityTimelineBinder);
    public void injectActivityStackedTimelineBinder(ActivityStackedTimelineBinder activityStackedTimelineBinder);
    public void injectEditTagNameDialogFragment(EditTagNameDialogFragment editTagNameDialogFragment);
    public void injectMainPageFragment(MainPageFragment fragment);
    public void injectTaskRecentActivity(TaskRecentActivity activity);
    public void injectViewTaskFragment(ViewTaskFragment fragment);
    public void injectEditTaskActivityDialogFragment(EditTaskActivityDialogFragment dialogFragment);
    public void injectTagListFragment(TagListFragment fragment);
    public void injectSettingsFragment(SettingsFragment fragment);
    public void injectImportStatsDialog(ImportStatsDialog dialog);
}
