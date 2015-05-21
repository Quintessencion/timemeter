package com.simbirsoft.timemeter.ui.stats;


import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.be.android.library.worker.util.JobSelector;
import com.google.common.collect.Sets;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.FilterableJob;
import com.simbirsoft.timemeter.jobs.LoadOverallTaskActivityTimeJob;
import com.simbirsoft.timemeter.jobs.LoadPeriodActivityTimelineJob;
import com.simbirsoft.timemeter.jobs.LoadPeriodSplitActivityTimelineJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.model.TaskLoadFilter;
import com.simbirsoft.timemeter.model.TaskOverallActivity;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.model.DailyActivityDuration;
import com.simbirsoft.timemeter.ui.model.DailyTaskActivityDuration;
import com.simbirsoft.timemeter.ui.stats.binders.ActivityStackedTimelineBinder;
import com.simbirsoft.timemeter.ui.stats.binders.ActivityTimelineBinder;
import com.simbirsoft.timemeter.ui.stats.binders.OverallActivityTimePieBinder;
import com.simbirsoft.timemeter.ui.views.ProgressLayout;
import com.squareup.otto.Bus;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;

import javax.inject.Inject;

@EFragment(R.layout.fragment_stats_details)
public class StatsDetailsFragment extends BaseFragment implements
        JobLoader.JobLoaderCallbacks {

    public static final String EXTRA_TASK_FILTER = "extra_task_filter";

    public static final String EXTRA_CHART_VIEW_TYPE = "extra_chart_view_type";

    private static final String STATS_LOADER_TAG = "StatsDetailsFragment_";

    private static final Logger LOG = LogFactory.getLogger(StatsDetailsFragment.class);

    private ActionBar mActionBar;

    @Inject
    Bus mBus;

    @ViewById(R.id.container)
    FrameLayout mContainer;

    @ViewById(R.id.progressLayout)
    ProgressLayout mProgressLayout;

    @FragmentArg(EXTRA_TASK_FILTER)
    TaskLoadFilter mExtraTaskFilter;

    @FragmentArg(EXTRA_CHART_VIEW_TYPE)
    int mChartViewType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injection.sUiComponent.injectStatsDetailsFragment(this);
        mBus.register(this);
    }

    @AfterViews
    void bindViews() {
        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mProgressLayout.setProgressLayoutCallbacks(
                new ProgressLayout.JobProgressLayoutCallbacks(JobSelector.forJobTags(STATS_LOADER_TAG)) {

                    @Override
                    public boolean hasContent() {
                        return mContainer != null && mContainer.getChildCount() > 0;
                    }

                });
        requestLoad(STATS_LOADER_TAG, this);
        mProgressLayout.updateProgressView();
    }

    @Override
    public void onDestroyView() {
        mBus.unregister(this);
        super.onDestroyView();
    }

    @OnJobSuccess(LoadOverallTaskActivityTimeJob.class)
    public void onOverallActivitiesLoaded(LoadJobResult<List<TaskOverallActivity>> result) {
        OverallActivityTimePieBinder binder = new OverallActivityTimePieBinder(result.getData());
        displayChart(binder);
    }

    @OnJobFailure(LoadOverallTaskActivityTimeJob.class)
    public void onOverallActivitiesLoadFailed() {
        LOG.error("statistics LoadOverallTaskActivityTimeJob failed");
    }

    @OnJobSuccess(LoadPeriodActivityTimelineJob.class)
    public void onPeriodActivitiesLoaded(LoadJobResult<List<DailyActivityDuration>> result) {
        ActivityTimelineBinder binder = new ActivityTimelineBinder(result.getData());
        displayChart(binder);
    }

    @OnJobFailure(LoadPeriodActivityTimelineJob.class)
    public void onPeriodActivitiesLoadFailed() {
        LOG.error("statistics LoadPeriodActivityTimelineJob failed");
    }

    @OnJobSuccess(LoadPeriodSplitActivityTimelineJob.class)
    public void onPeriodSplitActivitiesLoaded(LoadJobResult<List<DailyTaskActivityDuration>> result) {
        ActivityStackedTimelineBinder binder = new ActivityStackedTimelineBinder(result.getData());
        displayChart(binder);
    }

    @OnJobFailure(LoadPeriodSplitActivityTimelineJob.class)
    public void onPeriodSplitActivitiesLoadFailed() {
        LOG.error("statistics LoadPeriodSplitActivityTimelineJob failed");
    }

    @Override
    public Job onCreateJob(String s) {
        LoadJob job = getJob();
        job.setGroupId(JobManager.JOB_GROUP_UNIQUE);

        if (mExtraTaskFilter != null) {
            ((FilterableJob) job).getTaskLoadFilter()
                    .tags((mExtraTaskFilter.getFilterTags() != null) ? mExtraTaskFilter.getFilterTags() : Sets.newHashSet())
                    .dateMillis(mExtraTaskFilter.getDateMillis())
                    .period(mExtraTaskFilter.getPeriod())
                    .searchText(mExtraTaskFilter.getSearchText())
                    .taskIds(mExtraTaskFilter.getTaskIds());
        }

        job.addTag(STATS_LOADER_TAG);
        return job;
    }

    private LoadJob getJob() {
        switch (mChartViewType) {
            case StatisticsViewBinder.VIEW_TYPE_ACTIVITY_OVERALL_TIME_PIE:
                return Injection.sJobsComponent.loadOverallTaskActivityTimeJob();

            case StatisticsViewBinder.VIEW_TYPE_ACTIVITY_TIMELINE:
                return Injection.sJobsComponent.loadPeriodActivityTimelineJob();

            case StatisticsViewBinder.VIEW_TYPE_ACTIVITY_STACKED_TIMELINE:
                return Injection.sJobsComponent.loadPeriodSplitActivityTimelineJob();
        }
        throw new IllegalStateException("Illegal chart type");
    }

    private void displayChart(StatisticsViewBinder binder) {
        View view = binder.createView(getActivity(), mContainer, true);

        final String chartTitle = binder.getTitle();
        if (!TextUtils.isEmpty(chartTitle)) {
            mActionBar.setTitle(chartTitle);
        }

        binder.bindView(view);
        mContainer.removeAllViews();
        mContainer.addView(view);

        mProgressLayout.updateProgressView();
    }
}
