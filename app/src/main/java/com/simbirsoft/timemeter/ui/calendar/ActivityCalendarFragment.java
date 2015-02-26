package com.simbirsoft.timemeter.ui.calendar;

import android.content.res.Resources;
import android.view.ViewGroup;

import com.be.android.library.worker.annotations.OnJobEvent;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.main.MainPagerAdapter;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_activity_calendar)
public class ActivityCalendarFragment extends BaseFragment implements MainPagerAdapter.PageTitleProvider {

    @ViewById(R.id.calendarContentRoot)
    ViewGroup mCalendarContentRoot;

    @Override
    public String getPageTitle(Resources resources) {
        return resources.getString(R.string.title_activity_calendar);
    }

    @OnJobEvent
    public void onCalendarActivityLoaded() {

    }
}
