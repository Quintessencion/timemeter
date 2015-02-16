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
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadStatisticsViewBinders;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.main.MainPagerAdapter;
import com.simbirsoft.timemeter.ui.stats.binders.OverallActivityTimePieBinder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

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

    private StatsListAdapter mStatsListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        return Injection.sJobsComponent.loadStatisticsViewBinders();
    }

    @Override
    public String getPageTitle(Resources resources) {
        return resources.getString(R.string.title_stats);
    }
}
