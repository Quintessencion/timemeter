package com.simbirsoft.timemeter.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobLoaderManager;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.melnykov.fab.FloatingActionButton;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTaskListJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.base.FragmentContainerActivity;
import com.simbirsoft.timemeter.ui.taskedit.EditTaskFragment;
import com.simbirsoft.timemeter.ui.taskedit.EditTaskFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;

@EFragment(R.layout.fragment_task_list)
public class TaskListFragment extends BaseFragment implements JobLoader.JobLoaderCallbacks {

    private static final Logger LOG = LogFactory.getLogger(TaskListFragment.class);

    private static final int REQUEST_CODE_CREATE_TASK = 100;

    private static final int COLUMN_COUNT_DEFAULT = 2;

    public static TaskListFragment newInstance() {
        return new TaskListFragment_();
    }

    @ViewById(android.R.id.list)
    RecyclerView mTasksView;

    @ViewById(R.id.floatingButton)
    FloatingActionButton mFloatingButton;

    private TaskListAdapter mTasksViewAdapter;
    private String mLoaderAttachTag;
    private RecyclerView.LayoutManager mTasksViewLayoutManager;

    @Click(R.id.floatingButton)
    void onFloatingButtonClicked(View v) {
        LOG.info("floating button clicked");
        Bundle args = new Bundle();
        args.putString(EditTaskFragment.EXTRA_TITLE, getString(R.string.title_begin_new_task));
        Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                getActivity(), EditTaskFragment_.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, REQUEST_CODE_CREATE_TASK);
    }

    @LongClick(R.id.floatingButton)
    void onFloatingButtonLongClicked(View v) {
        showToast(R.string.hint_new_task);
    }

    @AfterViews
    void bindViews() {
        mFloatingButton.attachToRecyclerView(mTasksView);
        mTasksView.setHasFixedSize(true);

        mTasksViewLayoutManager = new StaggeredGridLayoutManager(
                COLUMN_COUNT_DEFAULT,
                StaggeredGridLayoutManager.VERTICAL);
        mTasksView.setLayoutManager(mTasksViewLayoutManager);

        mTasksViewAdapter = new TaskListAdapter();
        mTasksView.setAdapter(mTasksViewAdapter);

        requestLoad(mLoaderAttachTag, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLoaderAttachTag = getClass().getName() + "_loader_tag";
    }

    @OnJobSuccess(jobType = LoadTaskListJob.class)
    public void onTaskListLoaded(LoadJobResult<List<Task>> event) {
        mTasksViewAdapter.setTasks(event.getData());
    }

    @OnJobFailure(jobType = LoadTaskListJob.class)
    public void onTaskListLoadFailed() {
        showToast(R.string.error_unable_to_load_task_list);
    }

    @Override
    public Job onCreateJob(String loaderAttachTag) {
        return Injection.sJobsComponent.loadTaskListJob();
    }
}
