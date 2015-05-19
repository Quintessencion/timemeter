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
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadActivityCalendarJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.FragmentContainerActivity;
import com.simbirsoft.timemeter.ui.main.MainPageFragment;
import com.simbirsoft.timemeter.ui.main.MainPagerAdapter;
import com.simbirsoft.timemeter.ui.model.CalendarData;
import com.simbirsoft.timemeter.ui.model.CalendarPeriod;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.taskedit.EditTaskFragment;
import com.simbirsoft.timemeter.ui.taskedit.ViewTaskFragment;
import com.simbirsoft.timemeter.ui.taskedit.ViewTaskFragment_;
import com.simbirsoft.timemeter.ui.views.CalendarNavigationView;
import com.simbirsoft.timemeter.ui.views.CalendarViewPager;
import com.simbirsoft.timemeter.ui.views.WeekCalendarView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.Date;
import java.util.List;

@EFragment(R.layout.fragment_activity_calendar)
public class ActivityCalendarFragment extends MainPageFragment implements MainPagerAdapter.PageTitleProvider,
        JobLoader.JobLoaderCallbacks, CalendarNavigationView.OnCalendarNavigateListener,
        WeekCalendarView.OnCellClickListener, PopupWindow.OnDismissListener,
        CalendarPopupAdapter.TaskClickListener {

    private static final int REQUEST_CODE_EDIT_TASK = 100;

    private static final Logger LOG = LogFactory.getLogger(ActivityCalendarFragment.class);

    private static final String CALENDAR_LOADER_TAG = "ActivityCalendarFragment_calendar_loader";

    @ViewById(R.id.calendarScrollView)
    ScrollView mCalendarScrollView;

    @ViewById(R.id.calendarViewPager)
    CalendarViewPager mCalendarViewPager;


    @ViewById(R.id.calendarNavigationView)
    CalendarNavigationView mCalendarNavigationView;

    @InstanceState
    CalendarPeriod mCalendarPeriod;


    private CalendarPagerAdapter mPagerAdapter;
    private CalendarPopupHelper mPopupHelper;
    private long mScrollViewMillisOffset = -1;

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
        getBus().register(this);
    }

    @Override
    public void onDestroyView() {
        mPopupHelper.unregister();
        getBus().unregister(this);
        super.onDestroyView();
    }

    @Override
    public String getPageTitle(Resources resources) {
        return resources.getString(R.string.title_activity_calendar);
    }

    @Override
    protected void onFilterViewStateChanged() {
        JobManager.getInstance().cancelAll(JobSelector.forJobTags(CALENDAR_LOADER_TAG));
        saveScrollViewOffset();
        String loaderTag = getFilterLoaderTag(CALENDAR_LOADER_TAG);
        requestLoad(loaderTag, this);
    }

    @OnJobSuccess(LoadActivityCalendarJob.class)
    public void onCalendarActivityLoaded(LoadJobResult<CalendarData> result) {
        mCalendarPeriod = result.getData().getCalendarPeriod();
        mCalendarNavigationView.setCalendarPeriod(mCalendarPeriod);
        mPagerAdapter.setCurrentViewActivityCalendar(result.getData().getActivityCalendar());
        adjustScrollViewOffset();
    }

    @OnJobFailure(LoadActivityCalendarJob.class)
    public void onCalendarActivityLoadFailed() {
       showToast(R.string.error_unable_to_load_calendar_data);
    }

    @Override
    public Job onCreateJob(String s) {
        LoadActivityCalendarJob job = Injection.sJobsComponent.loadActivityCalendarJob();
        job.setGroupId(JobManager.JOB_GROUP_UNIQUE);

        if (mCalendarPeriod != null) {
            job.setPrevFilterDateMillis(mCalendarPeriod.getFilterDateMillis());
            job.setStartDate(mCalendarPeriod.getStartDate());
            job.setEndDate(mCalendarPeriod.getEndDate());
        }

        fillTaskLoadFilter(job.getTaskLoadFilter());
        job.addTag(CALENDAR_LOADER_TAG);
        return job;
    }

    @Override
    public void onMovedNext(Date newStartDate, Date newEndDate) {
        saveScrollViewOffset();
        mPagerAdapter.moveNext(newStartDate, newEndDate);
        requestLoad(newStartDate, newEndDate);
    }

    @Override
    public void onMovedPrev(Date newStartDate, Date newEndDate) {
        saveScrollViewOffset();
        mPagerAdapter.movePrev(newStartDate, newEndDate);
        requestLoad(newStartDate, newEndDate);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_EDIT_TASK:
                if (resultCode == EditTaskFragment.RESULT_CODE_CANCELLED) {
                    LOG.debug("result: task edit cancelled");
                    return;
                }
                switch (resultCode) {
                    case EditTaskFragment.RESULT_CODE_TASK_REMOVED:
                        LOG.debug("result: task removed");
                        if (mPopupHelper.isVisible()) {
                            mPopupHelper.dismiss();
                        }
                        mPagerAdapter.removeSpansFromCurrentView(data.getLongExtra(
                                EditTaskFragment.EXTRA_TASK_ID, -1));
                        showTaskRemoveUndoBar(data.getParcelableExtra(
                                EditTaskFragment.EXTRA_TASK_BUNDLE));
                        break;

                    case EditTaskFragment.RESULT_CODE_TASK_UPDATED:
                        LOG.debug("result: task updated");
                        if (mPopupHelper.isVisible()) {
                            mPopupHelper.updateTask(data.getParcelableExtra(
                                    EditTaskFragment.EXTRA_TASK_BUNDLE));
                        }
                        break;
                }
                sendTaskChangedEvent(resultCode);
                return;

            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveScrollViewOffset() {
        WeekCalendarView v = mPagerAdapter.getCurrentView();
        mScrollViewMillisOffset = (v != null) ? v.getMillisOffset(mCalendarScrollView.getScrollY()) : -1;
    }

    private void adjustScrollViewOffset() {
        WeekCalendarView v = mPagerAdapter.getCurrentView();
        if (mScrollViewMillisOffset < 0 || v == null) {
            return;
        }
        int offset = v.getPixelOffset(mScrollViewMillisOffset);
        if (mCalendarScrollView.getScrollY() != offset) {
            mCalendarScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mCalendarScrollView.scrollTo(0, offset);
                }
            });
        }
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

    @Override
    public void onCellClicked(Point point, List<TaskTimeSpan> spans) {
        point.offset(0, -mCalendarScrollView.getScrollY());
        mPopupHelper.show(mCalendarScrollView, point, spans);
    }

    @Override
    public void onDismiss() {
        mPagerAdapter.deselectCurrentViewCell();
    }

    @Override
    public void onTaskClicked(TaskBundle item) {
        Bundle args = new Bundle();
        args.putParcelable(ViewTaskFragment.EXTRA_TASK_BUNDLE, item);

        Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                getActivity(), ViewTaskFragment_.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, REQUEST_CODE_EDIT_TASK);
    }

    @Override
    protected boolean needUpdateAfterTaskChanged(int resultCode) {
        return resultCode == EditTaskFragment.RESULT_CODE_TASK_REMOVED;
    }

    @Override
    protected void reloadContent() {
        super.reloadContent();
        requestReload(CALENDAR_LOADER_TAG, this);
    }
}
