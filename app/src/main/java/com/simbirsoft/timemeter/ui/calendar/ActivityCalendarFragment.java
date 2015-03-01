package com.simbirsoft.timemeter.ui.calendar;

import android.content.res.Resources;
import android.view.ViewGroup;
import android.widget.TextView;

import com.be.android.library.worker.annotations.OnJobEvent;
import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadActivityCalendarJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.main.MainPagerAdapter;
import com.simbirsoft.timemeter.ui.model.ActivityCalendar;
import com.simbirsoft.timemeter.ui.views.WeekCalendarView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.Date;

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

    @AfterViews
    void bindViews() {
        requestLoad(CALENDAR_LOADER_TAG, this);
    }

    @Override
    public String getPageTitle(Resources resources) {
        return resources.getString(R.string.title_activity_calendar);
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
        job.setStartDate(new Date(/* 16 Feb */1424044800000L));
        job.setEndDate(new Date(/* 22 Feb */ 1424563200000L));

        // TODO: apply jobs filter (see TaskListFragment)

        return job;
    }
}
