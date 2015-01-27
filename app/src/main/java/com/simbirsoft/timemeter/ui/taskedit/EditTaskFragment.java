package com.simbirsoft.timemeter.ui.taskedit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.simbirsoft.timemeter.Consts;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.DatabaseHelper;
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
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.views.TagAutoCompleteTextView;
import com.tokenautocomplete.FilteredArrayAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;

import javax.inject.Inject;

@EFragment(R.layout.fragment_edit_task)
public class EditTaskFragment extends BaseFragment implements JobLoader.JobLoaderCallbacks,
        TagAutoCompleteTextView.TokenListener {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TASK_BUNDLE = "extra_task_bundle";

    private static final Logger LOG = LogFactory.getLogger(EditTaskFragment.class);

    private static final int REQUEST_CODE_DISCARD_CHANGES_AND_EXIT = 212;
    private static final int REQUEST_CODE_PERFORM_REMOVE_TASK = 222;

    public static final int RESULT_CODE_CANCELLED = Activity.RESULT_CANCELED;
    public static final int RESULT_CODE_TASK_CREATED = 1001;
    public static final int RESULT_CODE_TASK_UPDATED = 1002;
    public static final int RESULT_CODE_TASK_REMOVED = 1003;
    public static final int RESULT_CODE_TASK_RECREATED = 1004;

    @Inject
    DatabaseHelper mDatabaseHelper;

    @FragmentArg(EXTRA_TITLE)
    String mExtraTitle;

    @FragmentArg(EXTRA_TASK_ID)
    Long mExtraTaskId;

    @FragmentArg(EXTRA_TASK_BUNDLE)
    TaskBundle mExtraTaskBundle;

    @ViewById(android.R.id.edit)
    EditText mDescriptionEditView;

    @ViewById(R.id.tagSearchView)
    TagAutoCompleteTextView mTagAutoCompleteTextView;

    @ViewById(R.id.contentRoot)
    View mContentRoot;

    @InstanceState
    TaskBundle mTaskBundle;

    @InstanceState
    boolean mIsNewTask;

    private String mTagsLoaderAttachTag;
    private String mTaskBundleLoaderAttachTag;
    private Animation mProgressFadeIn;

    @TextChange(android.R.id.edit)
    void onTaskDescriptionChanged(TextView view) {
        mTaskBundle.getTask().setDescription(view.getText().toString());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Injection.sUiComponent.injectEditTaskFragment(this);

        setHasOptionsMenu(true);

        mTagsLoaderAttachTag = getClass().getName() + "__tags_loader";
        mTaskBundleLoaderAttachTag = getClass().getName() + "__task_tags_loader";
    }

    @Override
    public void onDestroy() {
        mTagAutoCompleteTextView.setTokenListener(null);
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_edit_task, menu);
    }

    private void displayRemoveTaskAlert() {
        Bundle args = AppAlertDialogFragment.prepareArgs(
                getActivity(),
                R.string.dialog_remove_task_warning_title,
                R.string.dialog_remove_task_warning_message,
                R.string.action_perform_remove,
                R.string.action_cancel);

        Intent launchIntent = DialogContainerActivity.prepareDialogLaunchIntent(
                getActivity(), AppAlertDialogFragment.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, REQUEST_CODE_PERFORM_REMOVE_TASK);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cancel:
                if (mTaskBundle.getTask().hasId()) {
                    displayRemoveTaskAlert();
                } else {
                    getActivity().finish();
                }

                return true;

            case android.R.id.home:
                LOG.debug("save task clicked");
                if (mTaskBundle.isEqualToSavedState() && mTaskBundle.getTask().hasId()) {
                    LOG.debug("task remain unchanged");
                    getActivity().finish();

                } else if (validateInput()) {
                    SaveTaskBundleJob job = Injection.sJobsComponent.saveTaskBundleJob();
                    job.setTaskBundle(mTaskBundle);
                    submitJob(job);
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(mTaskBundle.getTask().getDescription())) {
            SnackbarManager.show(Snackbar.with(getActivity())
                    .text(R.string.hint_task_description_is_empty)
                    .colorResource(R.color.lightRed)
                    .animation(false)
                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE));

            return false;
        }

        return true;
    }

    @OnJobSuccess(jobType = SaveTaskBundleJob.class)
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

    @OnJobFailure(jobType = SaveTaskBundleJob.class)
    public void onTaskSaveFailure() {
        showToast(R.string.error_unable_to_save_task);
    }

    @OnJobSuccess(jobType = RemoveTaskJob.class)
    public void onTaskRemoved() {
        Intent resultData = new Intent();
        resultData.putExtra(EXTRA_TASK_ID, mTaskBundle.getTask().getId());
        resultData.putExtra(EXTRA_TASK_BUNDLE, mTaskBundle);
        getActivity().setResult(RESULT_CODE_TASK_REMOVED, resultData);
        getActivity().finish();
    }

    @OnJobFailure(jobType = RemoveTaskJob.class)
    public void onTaskRemoveFailure() {
        showToast(R.string.error_unable_to_remove_task);
    }

    @AfterViews
    void bindViews() {
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (mExtraTitle != null) {
            actionBar.setTitle(mExtraTitle);
        }
        actionBar.setHomeAsUpIndicator(R.drawable.ic_action_accept);

        mTagAutoCompleteTextView.allowDuplicates(false);
        mTagAutoCompleteTextView.setTokenListener(this);

        mProgressFadeIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        mProgressFadeIn.setDuration(Consts.CONTENT_FADE_IN_DELAY_MILLIS);
        mProgressFadeIn.setFillBefore(true);
        mProgressFadeIn.setFillAfter(true);
        mContentRoot.setAnimation(mProgressFadeIn);

        // Load task bundle for existing task or create a new one
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

        // Load all tags for auto-complete view
        requestLoad(mTagsLoaderAttachTag, this);
    }

    @OnJobSuccess(jobType = LoadTagListJob.class)
    public void onTagListLoaded(LoadJobResult<List<Tag>> tags) {
        ArrayAdapter<Tag> adapter = new FilteredArrayAdapter<Tag>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                tags.getData()) {

            @Override
            protected boolean keepObject(Tag tag, String s) {
                return tag.getName().toLowerCase().contains(s.toLowerCase());
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Tag item = getItem(position);
                TextView view = (TextView) super.getView(position, convertView, parent);

                view.setText(item.getName());

                return view;
            }
        };

        mTagAutoCompleteTextView.setAdapter(adapter);
    }

    private void bindTaskBundleToViews() {
        mContentRoot.startAnimation(mProgressFadeIn);

        if (mTaskBundle == null) {
            return;
        }

        final Task task = mTaskBundle.getTask();
        if (!TextUtils.isEmpty(task.getDescription())) {
            mDescriptionEditView.setText(task.getDescription());
        }

        final List<Tag> tags = mTaskBundle.getTags();
        if (tags != null) {
            for (Tag tag : tags) {
                mTagAutoCompleteTextView.addObject(tag);
            }
        }
    }

    @OnJobSuccess(jobType = LoadTaskBundleJob.class)
    public void onTaskBundleLoaded(LoadJobResult<TaskBundle> taskBundle) {
        mTaskBundle = taskBundle.getData();
        mTaskBundle.saveState();

        bindTaskBundleToViews();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_DISCARD_CHANGES_AND_EXIT:
                if (resultCode == AppAlertDialogFragment.RESULT_CODE_ACCEPTED) {
                    getActivity().finish();
                    return;
                }
            case REQUEST_CODE_PERFORM_REMOVE_TASK:
                if (resultCode == AppAlertDialogFragment.RESULT_CODE_ACCEPTED) {
                    RemoveTaskJob removeJob = Injection.sJobsComponent.removeTaskJob();
                    removeJob.setTaskId(mTaskBundle.getTask().getId());
                    submitJob(removeJob);
                    return;
                }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean handleBackPress() {
        if (mTaskBundle.isEqualToSavedState()) {
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

        return true;
    }

    @Override
    public Job onCreateJob(String s) {
        if (mTaskBundleLoaderAttachTag.equals(s)) {
            LoadTaskBundleJob job = Injection.sJobsComponent.loadTaskBundleJob();
            job.setTaskId(mExtraTaskId);

            return job;
        }

        Job job = Injection.sJobsComponent.loadTagListJob();
        job.setGroupId(JobManager.JOB_GROUP_UNIQUE);

        return job;
    }

    @Override
    public void onTokenAdded(Object o) {
        Tag tag = (Tag) o;
        List<Tag> tags = mTaskBundle.getTags();
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    @Override
    public void onTokenRemoved(Object o) {
        mTaskBundle.getTags().remove(o);
    }
}
