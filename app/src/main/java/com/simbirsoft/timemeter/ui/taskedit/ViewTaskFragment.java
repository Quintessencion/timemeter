package com.simbirsoft.timemeter.ui.taskedit;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.be.android.library.worker.util.JobSelector;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTaskRecentActivitiesJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.activities.TaskActivitiesAdapter;
import com.simbirsoft.timemeter.ui.activities.TaskActivitiesFragment;
import com.simbirsoft.timemeter.ui.activities.TaskActivitiesFragment_;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.base.FragmentContainerActivity;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.model.TaskRecentActivity;
import com.simbirsoft.timemeter.ui.util.TagViewUtils;
import com.simbirsoft.timemeter.ui.views.ProgressLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.apmem.tools.layouts.FlowLayout;
import org.slf4j.Logger;

import java.util.List;

@EFragment(R.layout.fragment_view_task)
public class ViewTaskFragment extends BaseFragment
        implements JobLoader.JobLoaderCallbacks{
    public static final String EXTRA_TASK_BUNDLE = "extra_task_bundle";

    private static final String LOADER_TAG = "ViewTaskFragment";

    private static final Logger LOG = LogFactory.getLogger(ViewTaskFragment.class);

    private static final int REQUEST_CODE_EDIT_TASK = 100;
    private static final int REQUEST_CODE_VIEW_ACTIVITIES = 101;

    @FragmentArg(EXTRA_TASK_BUNDLE)
    TaskBundle mExtraTaskBundle;

    @ViewById(R.id.tagViewContainer)
    FlowLayout tagViewContainer;

    @ViewById(android.R.id.list)
    RecyclerView mRecyclerView;

    @ViewById(R.id.progressLayout)
    ProgressLayout mProgressLayout;

    @InstanceState
    int mListPosition;

    private TaskActivitiesAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_view_task, menu);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mAdapter.getItemCount() > 0) {
            mListPosition = ((LinearLayoutManager)
                    mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        }
    }

    public void bindTagViews(ViewGroup tagLayout, List<Tag> tags) {
        if ((tagLayout != null) && (tags != null)) {
            final int tagCount = tags.size();
            final View[] reuseViews = new View[tagCount];

            tagLayout.removeAllViewsInLayout();

            for (int i = 0; i < tagCount; i++) {
                reuseViews[i] = TagViewUtils.inflateTagView(
                        LayoutInflater.from(tagLayout.getContext()),
                        tagLayout,
                        0);
                tagLayout.addView(reuseViews[i]);
            }

            if (tagCount > 0) {
                for (int i = 0; i < tagCount; i++) {
                    Tag tag = tags.get(i);
                    TextView tagView = (TextView) reuseViews[i];
                    tagView.setText(tag.getName());
                    TagViewUtils.updateTagViewColor(tagView, tag.getColor());
                }
                tagLayout.setVisibility(View.VISIBLE);
            } else {
                tagLayout.setVisibility(View.GONE);
            }
        }
    }

    private void setActionBarTitleAndHome(String title) {
        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (title != null) {
            mActionBar.setTitle(title);
        }
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @AfterViews
    void bindViews() {
        setActionBarTitleAndHome(mExtraTaskBundle.getTask().getDescription());
        bindTagViews(tagViewContainer, mExtraTaskBundle.getTags());

        mRecyclerView.setHasFixedSize(false);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new TaskActivitiesAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mProgressLayout.setShouldDisplayEmptyIndicatorMessage(true);
        mProgressLayout.setEmptyIndicatorStyle(Typeface.ITALIC);
        final Resources res = getResources();
        mProgressLayout.setEmptyIndicatorTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen.empty_indicator_text_size));
        mProgressLayout.setEmptyIndicatorTextColor(res.getColor(R.color.empty_indicator));
        mProgressLayout.setProgressLayoutCallbacks(
                new ProgressLayout.JobProgressLayoutCallbacks(JobSelector.forJobTags(LOADER_TAG)) {
                    @Override
                    public boolean hasContent() {
                        return mAdapter.getItemCount() > 0;
                    }

                });
        requestLoad(LOADER_TAG, this);
        mProgressLayout.updateProgressView();
    }

    private void goToEditTask() {
        Bundle args = new Bundle();
        args.putString(EditTaskFragment.EXTRA_TITLE, getString(R.string.title_edit_task));
        args.putLong(EditTaskFragment.EXTRA_TASK_ID, mExtraTaskBundle.getTask().getId());

        Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                getActivity(), EditTaskFragment_.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, REQUEST_CODE_EDIT_TASK);
    }

    private void goToActivities() {
        Bundle args = new Bundle();
        String title = mExtraTaskBundle.getTask().getDescription();
        if(title != null) {
            args.putString(TaskActivitiesFragment.EXTRA_TITLE, title);
        }
        args.putLong(TaskActivitiesFragment.EXTRA_TASK_ID, mExtraTaskBundle.getTask().getId());

        Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                getActivity(), TaskActivitiesFragment_.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, REQUEST_CODE_VIEW_ACTIVITIES);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                LOG.debug("task edit clicked");
                goToEditTask();
                return true;

            case R.id.activities:
                LOG.debug("task view activities clicked");
                goToActivities();
                return true;

            case android.R.id.home:
                LOG.debug("task view home clicked");
                getActivity().finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        getActivity().setResult(resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_EDIT_TASK:
                if (resultCode == EditTaskFragment.RESULT_CODE_CANCELLED) {
                    LOG.debug("result: task edit cancelled");
                    return;
                }
                final TaskBundle bundle = data.getParcelableExtra(
                        EditTaskFragment.EXTRA_TASK_BUNDLE);

                switch (resultCode) {
                    case EditTaskFragment.RESULT_CODE_TASK_REMOVED:
                        LOG.debug("result: task removed");
                        getActivity().finish();
                        break;

                    case EditTaskFragment.RESULT_CODE_TASK_UPDATED:
                        LOG.debug("result: task updated");
                        bindTagViews(tagViewContainer, bundle.getTags());
                        setActionBarTitleAndHome(bundle.getTask().getDescription());
                        break;
                }
                return;

            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnJobSuccess(LoadTaskRecentActivitiesJob.class)
    public void onLoadSuccess(LoadJobResult<TaskRecentActivity> result) {
        TaskRecentActivity recentActivity = result.getData();
        mAdapter.setItems(recentActivity.getList());
        if (mListPosition != 0) {
            mRecyclerView.getLayoutManager().scrollToPosition(mListPosition);
            mListPosition = 0;
        }
        mProgressLayout.updateProgressView();
        if (mAdapter.getItemCount() == 0) {
            mProgressLayout.setEmptyIndicatorMessage(recentActivity.getEmptyIndicatorMessage(getResources()));
        } else {
            AlphaAnimation animation = new AlphaAnimation(0, 1);
            animation.setDuration(140);
            mRecyclerView.setAnimation(animation);
        }
    }

    @OnJobFailure(LoadTaskRecentActivitiesJob.class)
    public void onLoadFailed() {
        showToast(R.string.error_unable_to_load_task_activities);
    }

    @Override
    public Job onCreateJob(String s) {
        LoadTaskRecentActivitiesJob job = Injection.sJobsComponent.loadTaskRecentActivitiesJob();
        job.setGroupId(JobManager.JOB_GROUP_UNIQUE);
        job.setTaskId(mExtraTaskBundle.getTask().getId());
        job.addTag(LOADER_TAG);
        return job;
    }

    @Click(R.id.activitiesTitleContainer)
    void activitiesTitleClicked() {
        goToActivities();
    }
}