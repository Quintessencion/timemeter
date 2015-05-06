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
import com.simbirsoft.timemeter.events.FilterViewStateChangeEvent;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadStatisticsViewBinders;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.base.FragmentContainerActivity;
import com.simbirsoft.timemeter.ui.main.MainPagerAdapter;
import com.simbirsoft.timemeter.ui.main.MainPagerFragment;
import com.simbirsoft.timemeter.ui.model.TaskChangedEvent;
import com.simbirsoft.timemeter.ui.views.FilterView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;

import javax.inject.Inject;

@EFragment(R.layout.fragment_stats_list)
public class StatsListFragment extends BaseFragment implements
        JobLoader.JobLoaderCallbacks,
        MainPagerAdapter.PageTitleProvider,
        StatsListAdapter.ChartClickListener,
        MainPagerFragment.PageFragment {

    private static final Logger LOG = LogFactory.getLogger(StatsListFragment.class);

    private static final String STATISTICS_BINDER_LOADER_TAG = "StatsListFragment_";

    @ViewById(android.R.id.list)
    RecyclerView mRecyclerView;

    @ViewById(android.R.id.empty)
    TextView mEmptyStatusMessageView;

    @Inject
    Bus mBus;

    private StatsListAdapter mStatsListAdapter;
    private FilterView.FilterState mFilterViewState;
    private boolean mIsContentInvalidated;

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
        mBus.register(this);
    }

    @Override
    public void onDestroyView() {
        mBus.unregister(this);

        super.onDestroyView();
    }

    @Subscribe
    public void onFilterViewStateChanged(FilterViewStateChangeEvent ev) {
        mFilterViewState = ev.getFilterState();

        JobManager.getInstance().cancelAll(JobSelector.forJobTags(STATISTICS_BINDER_LOADER_TAG));

        String loaderTag = STATISTICS_BINDER_LOADER_TAG
                + "_filter:"
                + String.valueOf(mFilterViewState.hashCode());
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

        if (mFilterViewState != null) {
            job.getTaskLoadFilter()
                    .tags(mFilterViewState.tags)
                    .dateMillis(mFilterViewState.dateMillis)
                    .period(mFilterViewState.period)
                    .searchText(mFilterViewState.searchText);
        }

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
        if (mFilterViewState != null) {
            args.putParcelable(StatsDetailsFragment.EXTRA_TASK_FILTER, mFilterViewState);
        }
        args.putInt(StatsDetailsFragment.EXTRA_CHART_VIEW_TYPE, viewType);
        Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                getActivity(), StatsDetailsFragment_.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, 1000);

    }

    @Subscribe
    public void onTaskChanged(TaskChangedEvent event) {
        mIsContentInvalidated = true;
    }

    public void onSelected() {
        if (mIsContentInvalidated) {
            requestLoad(STATISTICS_BINDER_LOADER_TAG, this);
            mIsContentInvalidated = false;
        }
    }
}
