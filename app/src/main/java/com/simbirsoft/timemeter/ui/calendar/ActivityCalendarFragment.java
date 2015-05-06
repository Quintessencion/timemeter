package com.simbirsoft.timemeter.ui.calendar;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.widget.PopupWindow;
import android.widget.ScrollView;

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
import com.simbirsoft.timemeter.ui.base.FragmentContainerActivity;
import com.simbirsoft.timemeter.ui.main.MainPagerAdapter;
import com.simbirsoft.timemeter.ui.main.MainPagerFragment;
import com.simbirsoft.timemeter.ui.model.CalendarData;
import com.simbirsoft.timemeter.ui.model.CalendarPeriod;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.model.TaskChangedEvent;
import com.simbirsoft.timemeter.ui.taskedit.EditTaskFragment;
import com.simbirsoft.timemeter.ui.taskedit.ViewTaskFragment;
import com.simbirsoft.timemeter.ui.taskedit.ViewTaskFragment_;
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
        WeekCalendarView.OnCellClickListener, PopupWindow.OnDismissListener,
        CalendarPopupAdapter.TaskClickListener, MainPagerFragment.PageFragment {

    private static final int REQUEST_CODE_EDIT_TASK = 100;
    private static final int EVENT_SENDER_CODE = 3;

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
    private CalendarPopupHelper mPopupHelper;
    private boolean mIsContentInvalidated;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injection.sUiComponent.injectActivityCalendarFragment(this);
    }

    @AfterViews
    void bindViews() {
        mPopupHelper = new CalendarPopupHelper(getActivity());
        mPopupHelper.setOnDismissListener(this);
        mPopupHelper.setTaskClickListener(this);
        mPagerAdapter = new CalendarPagerAdapter(getActivity(), mCalendarViewPager, this);
        mCalendarNavigationView.setOnCalendarNavigateListener(this);
        requestLoad(CALENDAR_LOADER_TAG, this);
        mBus.register(this);
    }

    @Override
    public void onDestroyView() {
        mPopupHelper.unregister();
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

    @Subscribe
    public void onTaskChanged(TaskChangedEvent event) {
        if (event.getSender() != EVENT_SENDER_CODE
                && event.getResultCode() == EditTaskFragment.RESULT_CODE_TASK_REMOVED) {
            mIsContentInvalidated = true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_EDIT_TASK:
                if (resultCode == EditTaskFragment.RESULT_CODE_CANCELLED) {
                    LOG.debug("result: task edit cancelled");
                    return;
                }
                if (resultCode == EditTaskFragment.RESULT_CODE_TASK_REMOVED) {
                    LOG.debug("result: task removed");
                    final long taskId = data.getLongExtra(EditTaskFragment.EXTRA_TASK_ID, -1);
                    mPagerAdapter.removeSpansFromCurrentView(taskId);
                }
                mBus.post(new TaskChangedEvent(resultCode, EVENT_SENDER_CODE));
                return;

            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        mPopupHelper.show(mCalendarScrollView, point, spans);
    }

    public void onDismiss() {
        mPagerAdapter.deselectCurrentViewCell();
    }

    public void onTaskClicked(TaskBundle item) {
        mPopupHelper.dismiss();
        Bundle args = new Bundle();
        args.putParcelable(ViewTaskFragment.EXTRA_TASK_BUNDLE, item);

        Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                getActivity(), ViewTaskFragment_.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, REQUEST_CODE_EDIT_TASK);
    }

    public void onSelected() {
        if (mIsContentInvalidated) {
            requestLoad(CALENDAR_LOADER_TAG, this);
            mIsContentInvalidated = false;
        }
    }
}
