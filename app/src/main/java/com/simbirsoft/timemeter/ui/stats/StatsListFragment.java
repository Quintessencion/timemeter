package com.simbirsoft.timemeter.ui.stats;

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
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.events.FilterViewStateChangeEvent;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadPeriodSplitActivityTimelineJob;
import com.simbirsoft.timemeter.jobs.LoadStatisticsViewBinders;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.main.MainPagerAdapter;
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
        MainPagerAdapter.PageTitleProvider {

    private static final Logger LOG = LogFactory.getLogger(StatsListFragment.class);

    private static final String sStatisticsBinderLoaderAttachTag = "StatsListFragment_statistics_binder_loader";

    @ViewById(android.R.id.list)
    RecyclerView mRecyclerView;

    @ViewById(android.R.id.empty)
    TextView mEmptyStatusMessageView;

    @Inject
    Bus mBus;

    private StatsListAdapter mStatsListAdapter;
    private FilterView.FilterState mFilterViewState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Injection.sUiComponent.injectStatsListFragment(this);

        mStatsListAdapter = new StatsListAdapter();
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

        requestLoad(sStatisticsBinderLoaderAttachTag, this);
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

        Job job = JobManager.getInstance().findJob(sStatisticsBinderLoaderAttachTag);
        if (job != null) {
            JobManager.getInstance().cancelJob(job.getJobId());
        }

        requestLoad(String.valueOf(mFilterViewState.hashCode()), this);

        requestLoad(sStatisticsBinderLoaderAttachTag, this);
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
        if (mFilterViewState != null) {
            job.getTaskLoadFilter()
                    .tags(mFilterViewState.tags)
                    .dateMillis(mFilterViewState.dateMillis)
                    .period(mFilterViewState.period)
                    .searchText(mFilterViewState.searchText);
        }

        job.addTag(sStatisticsBinderLoaderAttachTag);

        return job;
    }

    @Override
    public String getPageTitle(Resources resources) {
        return resources.getString(R.string.title_stats);
    }
}
