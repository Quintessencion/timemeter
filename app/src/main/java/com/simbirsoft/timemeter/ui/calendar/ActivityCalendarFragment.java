package com.simbirsoft.timemeter.ui.calendar;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.be.android.library.worker.annotations.OnJobEvent;
import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.be.android.library.worker.util.JobSelector;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.events.FilterViewStateChangeEvent;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadActivityCalendarJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.main.MainPagerAdapter;
import com.simbirsoft.timemeter.ui.model.ActivityCalendar;
import com.simbirsoft.timemeter.ui.stats.StatsListAdapter;
import com.simbirsoft.timemeter.ui.views.FilterView;
import com.simbirsoft.timemeter.ui.views.WeekCalendarView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.Date;

import javax.inject.Inject;

@EFragment(R.layout.fragment_activity_calendar)
public class ActivityCalendarFragment extends BaseFragment implements MainPagerAdapter.PageTitleProvider,
        JobLoader.JobLoaderCallbacks {

    private static final Logger LOG = LogFactory.getLogger(ActivityCalendarFragment.class);

    private static final String CALENDAR_LOADER_TAG = "ActivityCalendarFragment_calendar_loader";

    @ViewById(R.id.calendarContentRoot)
    ViewGroup mCalendarContentRoot;

    @ViewById(R.id.calendarView)
    WeekCalendarView mWeekCalendarView;

    @ViewById(android.R.id.empty)
    TextView mEmptyIndicatorView;

    @InstanceState
    FilterView.FilterState mFilterViewState;

    @Inject
    Bus mBus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injection.sUiComponent.injectActivityCalendarFragment(this);
    }

    @AfterViews
    void bindViews() {
        mEmptyIndicatorView.setVisibility(View.GONE);
        requestLoad(CALENDAR_LOADER_TAG, this);
        mBus.register(this);
    }

    @Override
    public void onDestroyView() {
        mBus.unregister(this);
        super.onDestroyView();
    }

    @Override
    public String getPageTitle(Resources resources) {
        return resources.getString(R.string.title_activity_calendar);
    }

    @Subscribe
    public void onFilterViewStateChanged(FilterViewStateChangeEvent ev) {
        if (!isAdded()) {
            return;
        }
        mFilterViewState = ev.getFilterState();

        JobManager.getInstance().cancelAll(JobSelector.forJobTags(CALENDAR_LOADER_TAG));

        String loaderTag = CALENDAR_LOADER_TAG
                + "filter:"
                + String.valueOf(mFilterViewState.hashCode());
        requestLoad(loaderTag, this);
    }

    @OnJobSuccess(LoadActivityCalendarJob.class)
    public void onCalendarActivityLoaded(LoadJobResult<ActivityCalendar> result) {
        mWeekCalendarView.setActivityCalendar(result.getData());
    }

    @OnJobFailure(LoadActivityCalendarJob.class)
    public void onCalendarActivityLoadFailed() {
        // TODO: display error explanation message
        LOG.error("failed to load activity calendar");
    }

    @Override
    public Job onCreateJob(String s) {
        LoadActivityCalendarJob job = Injection.sJobsComponent.loadActivityCalendarJob();
        job.setGroupId(JobManager.JOB_GROUP_UNIQUE);
        job.setPrevFilterDateMillis((mWeekCalendarView != null && mWeekCalendarView.getActivityCalendar() != null)
                ? mWeekCalendarView.getActivityCalendar().getFilterDateMillis() : 0);
        //job.setStartDate(new Date(/* 16 Feb */1424044800000L));
        //job.setEndDate(new Date(/* 22 Feb */ 1424563200000L));

        if (mFilterViewState != null) {
            job.getTaskLoadFilter()
                    .tags(mFilterViewState.tags)
                    .dateMillis(mFilterViewState.dateMillis)
                    .period(mFilterViewState.period)
                    .searchText(mFilterViewState.searchText);
        }
        job.addTag(CALENDAR_LOADER_TAG);
        return job;
    }
}
