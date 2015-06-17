package com.simbirsoft.timemeter.ui.settings;

import android.os.Bundle;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.handlers.JobEventDispatcher;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTaskListJob;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.model.TaskBundle;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

import java.util.List;

@EFragment(R.layout.fragment_load_tasks)
public class LoadTasksFragment extends BaseFragment implements JobLoader.JobLoaderCallbacks {

    private static final int LOAD_TASK_JOB_ID = 2970017;

    private static final String TASK_LIST_LOADER_TAG = "SettingsFragment_taskLoaderTag";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        requestLoad(TASK_LIST_LOADER_TAG, this);
    }

    @AfterViews
    public void bindViews() {
        requestLoad(TASK_LIST_LOADER_TAG, this);
    }

    @Override
    public Job onCreateJob(String loaderAttachTag) {
        LoadTaskListJob job = Injection.sJobsComponent.loadTaskListJob();
        job.setGroupId(LOAD_TASK_JOB_ID);

        //fillTaskLoadFilter(job.getTaskLoadFilter());
        job.addTag(loaderAttachTag);

        return job;
    }

    @OnJobSuccess(LoadTaskListJob.class)
    public void onTaskListLoaded(LoadJobResult<List<TaskBundle>> event) {
        List<TaskBundle> taskBundles = event.getData();

        int i = 5;
    }

    @OnJobFailure(LoadTaskListJob.class)
    public void onTaskListLoadFailed() {
        int i = 5;
    }

    public void loadTasks() {
        //requestLoad(TASK_LIST_LOADER_TAG, this);
    }
}
