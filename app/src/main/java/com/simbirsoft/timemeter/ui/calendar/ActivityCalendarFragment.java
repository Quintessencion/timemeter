package com.simbirsoft.timemeter.ui.calendar;

import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.be.android.library.worker.util.JobSelector;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.events.FilterViewStateChangeEvent;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadActivityCalendarJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.main.MainPagerAdapter;
import com.simbirsoft.timemeter.ui.model.ActivityCalendar;
import com.simbirsoft.timemeter.ui.model.CalendarData;
import com.simbirsoft.timemeter.ui.model.CalendarPeriod;
import com.simbirsoft.timemeter.ui.views.CalendarPopupHelper;
import com.simbirsoft.timemeter.ui.views.CalendarViewPager;
import com.simbirsoft.timemeter.ui.views.FilterView;
import com.simbirsoft.timemeter.ui.views.CalendarNavigationView;
import com.simbirsoft.timemeter.ui.views.WeekCalendarView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

@EFragment(R.layout.fragment_activity_calendar)
public class ActivityCalendarFragment extends BaseFragment implements MainPagerAdapter.PageTitleProvider,
        JobLoader.JobLoaderCallbacks, CalendarNavigationView.OnCalendarNavigateListener,
        WeekCalendarView.OnCellClickListener, PopupWindow.OnDismissListener {

    private static final Logger LOG = LogFactory.getLogger(ActivityCalendarFragment.class);

    private static final String CALENDAR_LOADER_TAG = "ActivityCalendarFragment_calendar_loader";

    @ViewById(R.id.calendarScrollView)
    ScrollView mCalendarScrollView;

    @ViewById(R.id.calendarViewPager)
    CalendarViewPager mCalendarViewPager;


    @ViewById(R.id.calendarNavigationView)
    CalendarNavigationView mCalendarNavigationView;

    @InstanceState
    FilterView.FilterState mFilterViewState;

    @InstanceState
    CalendarPeriod mCalendarPeriod;

    @Inject
    Bus mBus;

    private CalendarPagerAdapter mPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injection.sUiComponent.injectActivityCalendarFragment(this);
    }

    @AfterViews
    void bindViews() {
        mPagerAdapter = new CalendarPagerAdapter(getActivity(), mCalendarViewPager, this);
        mCalendarNavigationView.setOnCalendarNavigateListener(this);
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
    public void onCalendarActivityLoaded(LoadJobResult<CalendarData> result) {
        mCalendarPeriod = result.getData().getCalendarPeriod();
        mCalendarNavigationView.setCalendarPeriod(mCalendarPeriod);
        mPagerAdapter.setCurrentViewActivityCalendar(result.getData().getActivityCalendar());
    }

    @OnJobFailure(LoadActivityCalendarJob.class)
    public void onCalendarActivityLoadFailed() {
        // TODO: display error explanation message
        LOG.error("failed to load activity calendar");
    }

    @Override
    public Job onCreateJob(String s) {
        LOG.debug("Calendrr create job");
        LoadActivityCalendarJob job = Injection.sJobsComponent.loadActivityCalendarJob();
        job.setGroupId(JobManager.JOB_GROUP_UNIQUE);

        if (mCalendarPeriod != null) {
            job.setPrevFilterDateMillis(mCalendarPeriod.getFilterDateMillis());
            job.setStartDate(mCalendarPeriod.getStartDate());
            job.setEndDate(mCalendarPeriod.getEndDate());
        }

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

    @Override
    public void onMovedNext(Date newStartDate, Date newEndDate) {
        mPagerAdapter.moveNext(newStartDate, newEndDate);
        requestLoad(newStartDate, newEndDate);
    }

    @Override
    public void onMovedPrev(Date newStartDate, Date newEndDate) {
        mPagerAdapter.movePrev(newStartDate, newEndDate);
        requestLoad(newStartDate, newEndDate);
    }

    private void requestLoad(Date newStartDate, Date newEndDate) {
        JobManager.getInstance().cancelAll(JobSelector.forJobTags(CALENDAR_LOADER_TAG));

        String loaderTag = CALENDAR_LOADER_TAG
                + "period:"
                + String.valueOf(newStartDate.getTime())
                + "_"
                + String.valueOf(newEndDate.getTime());
        requestLoad(loaderTag, this);
    }

    public void onCellClicked(Point point, List<TaskTimeSpan> spans) {
        point.offset(0, -mCalendarScrollView.getScrollY());
        point.y = Math.min(mCalendarScrollView.getHeight(), Math.max(0, point.y));
        CalendarPopupHelper helper = new CalendarPopupHelper(getActivity());
        helper.setOnDismissListener(this);
        helper.show(mCalendarScrollView, point);
    }

    public void onDismiss() {
        mPagerAdapter.deselectCurrentViewCell();
    }
}
