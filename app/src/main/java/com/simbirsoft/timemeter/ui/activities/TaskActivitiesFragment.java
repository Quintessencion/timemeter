package com.simbirsoft.timemeter.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTaskActivitiesJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.model.TaskActivityItem;
import com.squareup.otto.Bus;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;

import javax.inject.Inject;

@EFragment(R.layout.fragment_task_activities)
public class TaskActivitiesFragment extends BaseFragment implements
        JobLoader.JobLoaderCallbacks {
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TITLE = "extra_title";

    private static final String LOADER_TAG = "TaskActivitiesFragment_";

    private static final Logger LOG = LogFactory.getLogger(TaskActivitiesFragment.class);

    @ViewById(android.R.id.list)
    RecyclerView mRecyclerView;


    @FragmentArg(EXTRA_TASK_ID)
    Long mExtraTaskId;

    @FragmentArg(EXTRA_TITLE)
    String mExtraTitle;

    @Inject
    Bus mBus;

    private ActionBar mActionBar;
    private TaskActivitiesAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injection.sUiComponent.injectTaskActivitiesFragment(this);
        mBus.register(this);
    }

    @Override
    public void onDestroyView() {
        mBus.unregister(this);
        super.onDestroyView();
    }

    @AfterViews
    void bindViews() {
        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        if (mExtraTitle != null) {
           mActionBar.setTitle(mExtraTitle);
        }

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new TaskActivitiesAdapter();
        mRecyclerView.setAdapter(mAdapter);
        requestLoad(LOADER_TAG, this);
    }

    @OnJobSuccess(LoadTaskActivitiesJob.class)
    public void onLoadSuccess(LoadJobResult<List<TaskActivityItem>> result) {
        mAdapter.setItems(result.getData());
    }

    @OnJobFailure(LoadTaskActivitiesJob.class)
    public void onLoadFailed() {
        LOG.error("LoadTaskActivitiesJob failed");
    }

    @Override
    public Job onCreateJob(String s) {
        LoadTaskActivitiesJob job = Injection.sJobsComponent.loadTaskActivitiesJob();
        job.setGroupId(JobManager.JOB_GROUP_UNIQUE);
        job.setTaskId(mExtraTaskId);
        job.addTag(LOADER_TAG);
        return job;
    }

}
