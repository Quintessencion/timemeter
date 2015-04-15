package com.simbirsoft.timemeter.ui.taskedit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transitions.everywhere.ChangeBounds;
import android.transitions.everywhere.Fade;
import android.transitions.everywhere.Scene;
import android.transitions.everywhere.TransitionManager;
import android.transitions.everywhere.TransitionSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.EventListener;
import com.simbirsoft.timemeter.Consts;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.controller.ActiveTaskInfo;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTagListJob;
import com.simbirsoft.timemeter.jobs.LoadTaskBundleJob;
import com.simbirsoft.timemeter.jobs.RemoveTaskJob;
import com.simbirsoft.timemeter.jobs.SaveTaskBundleJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.AppAlertDialogFragment;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.base.DialogContainerActivity;
import com.simbirsoft.timemeter.ui.base.FragmentContainerActivity;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.tags.TagListAdapter;
import com.simbirsoft.timemeter.ui.tasklist.TaskListFragment;
import com.simbirsoft.timemeter.ui.util.KeyboardUtils;
import com.simbirsoft.timemeter.ui.util.TagViewUtils;
import com.simbirsoft.timemeter.ui.util.TimerTextFormatter;
import com.simbirsoft.timemeter.ui.views.TagAutoCompleteTextView;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.apmem.tools.layouts.FlowLayout;
import org.slf4j.Logger;

import java.util.List;
import java.util.Stack;

@EFragment(R.layout.fragment_view_task)
public class ViewTaskFragment extends BaseFragment implements JobLoader.JobLoaderCallbacks {
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TASK_BUNDLE = "extra_task_bundle";

    private static final Logger LOG = LogFactory.getLogger(ViewTaskFragment.class);

    public static final int RESULT_CODE_CANCELLED = Activity.RESULT_CANCELED;
    public static final int RESULT_CODE_TASK_CREATED = 1001;
    public static final int RESULT_CODE_TASK_UPDATED = 1002;
    public static final int RESULT_CODE_TASK_REMOVED = 1003;
    public static final int RESULT_CODE_TASK_RECREATED = 1004;

    @FragmentArg(EXTRA_TITLE)
    String mExtraTitle;

    @FragmentArg(EXTRA_TASK_ID)
    Long mExtraTaskId;

    @FragmentArg(EXTRA_TASK_BUNDLE)
    TaskBundle mExtraTaskBundle;

    @ViewById(R.id.rootScene)
    ViewGroup mContentRoot;

    @InstanceState
    TaskBundle mTaskBundle;

    @InstanceState
    boolean mIsNewTask;

    @InstanceState
    String mTagFilter;

    private TaskViewScene mTaskViewScene;
    private String mTagsLoaderAttachTag;
    private String mTaskBundleLoaderAttachTag;
    private TagListAdapter mTagListAdapter;
    private Scene mCurrentScene;
    private ActionBar mActionBar;

    private final Stack<View> mReuseTagViews = new Stack<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mTagsLoaderAttachTag = getClass().getName() + "__tags_loader";
        mTaskBundleLoaderAttachTag = getClass().getName() + "__task_tags_loader";
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_view_task, menu);
    }

    private void bindTaskBundleToViews() {
        if (mTaskBundle == null) {
            return;
        }

        bindTagViews(mTaskViewScene.tagViewContainer, mTaskBundle.getTags());
    }

    public void bindTagViews(ViewGroup tagLayout, List<Tag> tags) {
        final int tagCount = tags.size();
        final View[] reuseViews = new View[tagCount];

        final int reuseViewCount = tagLayout.getChildCount();
        for (int i = 0; i < reuseViewCount; i++) {
            mReuseTagViews.add(tagLayout.getChildAt(i));
        }
        tagLayout.removeAllViewsInLayout();

        for (int i = 0; i < tagCount; i++) {
            if (mReuseTagViews.isEmpty()) {
                reuseViews[i] = TagViewUtils.inflateTagView(
                        LayoutInflater.from(tagLayout.getContext()),
                        tagLayout,
                        0);
            } else {
                reuseViews[i] = mReuseTagViews.pop();
            }

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

    private TaskViewScene createRootScene() {
        TaskViewScene scene = TaskViewScene.create(getActivity(), mContentRoot);
        return scene;
    }

    private void goToMainScene() {
        if (mTaskViewScene != null && mCurrentScene == mTaskViewScene.scene) {
            return;
        }

        //View focusView = getActivity().getCurrentFocus();
        //if (focusView != null) {
        //    KeyboardUtils.hideSoftInput(getActivity(), focusView.getWindowToken());
        //}

        if (mTaskViewScene != null) {
            mContentRoot.removeView(mTaskViewScene.layout);
        }

        mTaskViewScene = createRootScene();
        mCurrentScene = mTaskViewScene.scene;

        if (mTaskBundle == null) {
            if (mExtraTaskBundle != null) {
                mTaskBundle = mExtraTaskBundle;

            } else if (mExtraTaskId != null) {
                requestLoad(mTaskBundleLoaderAttachTag, this);

            } else {
                mTaskBundle = TaskBundle.create();
                mTaskBundle.saveState();
                mIsNewTask = true;
            }
        }
        bindTaskBundleToViews();

        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(new Fade(Fade.IN));
        transitionSet.addTransition(new ChangeBounds());
        transitionSet.setOrdering(TransitionSet.ORDERING_TOGETHER);
        transitionSet.setDuration(Consts.CONTENT_FADE_IN_DELAY_MILLIS);
        transitionSet.setInterpolator(new DecelerateInterpolator());
        TransitionManager.go(mTaskViewScene.scene, transitionSet);

        mActionBar.setDisplayHomeAsUpEnabled(true);
        //mActionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
    }

    @AfterViews
    void bindViews() {
        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (mExtraTitle != null) {
            mActionBar.setTitle(mExtraTitle);
        }
        //mActionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
        goToMainScene();
    }

    private void goToEditTask() {
        Bundle args = new Bundle();
        args.putString(EditTaskFragment.EXTRA_TITLE, getString(R.string.title_edit_task));
        args.putLong(EditTaskFragment.EXTRA_TASK_ID, mExtraTaskId);

        Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                getActivity(), EditTaskFragment_.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, TaskListFragment.REQUEST_CODE_EDIT_TASK);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mTaskBundle == null) {
            return super.onOptionsItemSelected(item);
        }

        switch (item.getItemId()) {

            case R.id.edit:
                if (mTaskBundle.getTask().hasId()) {
                    goToEditTask();
                } else {
                    getActivity().finish();
                }
                return true;

            case android.R.id.home:
                LOG.debug("task view home clicked");
                getActivity().finish();
                /*if (mTaskTagsEditScene != null && mCurrentScene == mTaskTagsEditScene.scene) {
                    goToMainScene();
                    return true;
                }

                LOG.debug("save task clicked");
                if (mTaskBundle.isEqualToSavedState() && mTaskBundle.getTask().hasId()) {
                    LOG.debug("task remain unchanged");
                    getActivity().finish();

                } else if (validateInput()) {
                    SaveTaskBundleJob job = Injection.sJobsComponent.saveTaskBundleJob();
                    job.setTaskBundle(mTaskBundle);
                    submitJob(job);
                }*/

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnJobSuccess(SaveTaskBundleJob.class)
    public void onTaskSaved(SaveTaskBundleJob.SaveTaskResult result) {
        Intent resultData = new Intent();
        resultData.putExtra(EXTRA_TASK_ID, result.getTaskId());
        resultData.putExtra(EXTRA_TASK_BUNDLE, mTaskBundle);

        final Activity activity = getActivity();
        if (mIsNewTask) {
            activity.setResult(RESULT_CODE_TASK_CREATED, resultData);

        } else if (mExtraTaskBundle != null) {
            activity.setResult(RESULT_CODE_TASK_RECREATED, resultData);

        } else {
            activity.setResult(RESULT_CODE_TASK_UPDATED, resultData);
        }
        activity.finish();
    }

    @OnJobFailure(SaveTaskBundleJob.class)
    public void onTaskSaveFailure() {
        showToast(R.string.error_unable_to_save_task);
    }

    @OnJobSuccess(RemoveTaskJob.class)
    public void onTaskRemoved() {
        Intent resultData = new Intent();
        resultData.putExtra(EXTRA_TASK_ID, mTaskBundle.getTask().getId());
        resultData.putExtra(EXTRA_TASK_BUNDLE, mTaskBundle);
        getActivity().setResult(RESULT_CODE_TASK_REMOVED, resultData);
        getActivity().finish();
    }

    @OnJobFailure(RemoveTaskJob.class)
    public void onTaskRemoveFailure() {
        showToast(R.string.error_unable_to_remove_task);
    }

    @OnJobSuccess(LoadTagListJob.class)
    public void onTagListLoaded(LoadJobResult<List<Tag>> tags) {
        if (mTagListAdapter != null) {
            mTagListAdapter.setOriginItems(tags.getData());
            mTagListAdapter.getFilter().excludeTags(mTaskBundle.getTags()).filter(mTagFilter);
        }
    }

    @OnJobSuccess(LoadTaskBundleJob.class)
    public void onTaskBundleLoaded(LoadJobResult<TaskBundle> taskBundle) {
         mTaskBundle = taskBundle.getData();
        mTaskBundle.saveState();

        bindTaskBundleToViews();
    }

    @OnJobFailure(LoadTaskBundleJob.class)
    public void onTaskBundleLoadFailure() {
        SnackbarManager.show(Snackbar.with(getActivity())
                .text(R.string.error_loading_data)
                .colorResource(R.color.lightRed)
                .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                .eventListener(new EventListener() {
                    @Override
                    public void onShow(Snackbar snackbar) {
                    }

                    @Override
                    public void onShown(Snackbar snackbar) {
                    }

                    @Override
                    public void onDismiss(Snackbar snackbar) {
                    }

                    @Override
                    public void onDismissed(Snackbar snackbar) {
                        if (isAdded()) {
                            getActivity().finish();
                        }
                    }
                }));
    }

    @Override
    public boolean handleBackPress() {
        getActivity().finish();
        /*
        if (mTaskTagsEditScene != null && mCurrentScene == mTaskTagsEditScene.scene) {
            goToMainScene();
            return true;
        }

        if (mTaskBundle == null || mTaskBundle.isEqualToSavedState()) {
            return super.handleBackPress();
        }

        // There are unsaved changes - show alert
        Bundle args = AppAlertDialogFragment.prepareArgs(getActivity(),
                R.string.dialog_cancel_task_changes_warning_title,
                R.string.dialog_cancel_task_changes_warning_message,
                R.string.action_accept_yes,
                R.string.action_cancel);

        Intent launchIntent = DialogContainerActivity.prepareDialogLaunchIntent(
                getActivity(),
                AppAlertDialogFragment.class.getName(),
                args);
        getActivity().startActivityForResult(launchIntent, REQUEST_CODE_DISCARD_CHANGES_AND_EXIT);
*/
        return true;
    }

    @Override
    public Job onCreateJob(String s) {
        if (mTaskBundleLoaderAttachTag.equals(s)) {
            LoadTaskBundleJob job = Injection.sJobsComponent.loadTaskBundleJob();
            job.setTaskId(mExtraTaskId);
            job.setGroupId(JobManager.JOB_GROUP_UNIQUE);

            return job;
        }

        Job job = Injection.sJobsComponent.loadTagListJob();
        job.setGroupId(JobManager.JOB_GROUP_UNIQUE);

        return job;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        getActivity().setResult(resultCode, data);
        getActivity().finish();
    }
}