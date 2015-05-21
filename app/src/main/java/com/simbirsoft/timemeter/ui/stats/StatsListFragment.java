package com.simbirsoft.timemeter.ui.stats;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.be.android.library.worker.util.JobSelector;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadStatisticsViewBinders;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.FragmentContainerActivity;
import com.simbirsoft.timemeter.ui.main.MainPageFragment;
import com.simbirsoft.timemeter.ui.main.MainPagerAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;

@EFragment(R.layout.fragment_stats_list)
public class StatsListFragment extends MainPageFragment implements
        JobLoader.JobLoaderCallbacks,
        MainPagerAdapter.PageTitleProvider,
        StatsListAdapter.ChartClickListener {

    private static final String STATISTICS_BINDER_LOADER_TAG = "StatsListFragment_";

    @ViewById(android.R.id.list)
    RecyclerView mRecyclerView;

    @ViewById(android.R.id.empty)
    TextView mEmptyStatusMessageView;

    private StatsListAdapter mStatsListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Injection.sUiComponent.injectStatsListFragment(this);

        mStatsListAdapter = new StatsListAdapter();
        mStatsListAdapter.setChartClickListener(this);
    }

    @AfterViews
    void bindViews() {
        mEmptyStatusMessageView.setVisibility(View.GONE);
        mRecyclerView.setHasFixedSize(false);

        RecyclerView.LayoutManager statsLayoutManager = new StaggeredGridLayoutManager(
                1,
                StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(statsLayoutManager);
        mRecyclerView.setAdapter(mStatsListAdapter);

        requestLoad(STATISTICS_BINDER_LOADER_TAG, this);
        getBus().register(this);
    }

    @Override
    public void onDestroyView() {
        getBus().unregister(this);

        super.onDestroyView();
    }

    @Override
    protected void onFilterViewStateChanged() {
        JobManager.getInstance().cancelAll(JobSelector.forJobTags(STATISTICS_BINDER_LOADER_TAG));

        String loaderTag = getFilterLoaderTag(STATISTICS_BINDER_LOADER_TAG);
        requestLoad(loaderTag, this);
    }

    @OnJobSuccess(LoadStatisticsViewBinders.class)
    public void onStatisticsViewBindersLoaded(LoadJobResult<List<StatisticsViewBinder>> result) {
        mStatsListAdapter.setViewBinders(result.getData());
    }

    @OnJobFailure(LoadStatisticsViewBinders.class)
    public void onStatisticsViewBindersLoadFailed() {
        LOG.error("statistics load failed");
    }

    @Override
    public Job onCreateJob(String s) {
        LoadStatisticsViewBinders job = Injection.sJobsComponent.loadStatisticsViewBinders();
        job.setGroupId(JobManager.JOB_GROUP_UNIQUE);

        fillTaskLoadFilter(job.getTaskLoadFilter());
        job.addTag(STATISTICS_BINDER_LOADER_TAG);
        return job;
    }

    @Override
    public String getPageTitle(Resources resources) {
        return resources.getString(R.string.title_stats);
    }

    @Override
    public void onChartClicked(int viewType) {
        Bundle args = new Bundle();
        if (hasFilter()) {
            args.putParcelable(StatsDetailsFragment.EXTRA_TASK_FILTER, getFilterViewState());
        }
        args.putInt(StatsDetailsFragment.EXTRA_CHART_VIEW_TYPE, viewType);
        Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                getActivity(), StatsDetailsFragment_.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, 1000);

    }

    @Override
    protected void reloadContent() {
        super.reloadContent();
        requestReload(STATISTICS_BINDER_LOADER_TAG, this);
    }

    @Override
    protected Logger createLogger() {
        return LogFactory.getLogger(StatsListFragment.class);
    }

    @Override
    protected void onTaskProcessed(Intent data) {
        super.onTaskProcessed(data);

        if (!isSelected()) {
            invalidateContent();
        }
    }
}
