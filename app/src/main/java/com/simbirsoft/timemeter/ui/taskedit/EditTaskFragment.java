package com.simbirsoft.timemeter.ui.taskedit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.jobs.CallableJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTagListJob;
import com.simbirsoft.timemeter.jobs.LoadTaskBundleJob;
import com.simbirsoft.timemeter.jobs.LoadTaskListJob;
import com.simbirsoft.timemeter.jobs.LoadTaskTagsJob;
import com.simbirsoft.timemeter.ui.base.AppAlertDialogFragment;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.base.DialogContainerActivity;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.views.TagAutoCompleteTextView;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.tokenautocomplete.TokenCompleteTextView;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

@EFragment(R.layout.fragment_edit_task)
public class EditTaskFragment extends BaseFragment implements JobLoader.JobLoaderCallbacks,
        TagAutoCompleteTextView.TokenListener {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_TASK_ID = "extra_task_id";

    private static final int REQUEST_CODE_DISCARD_CHANGES_AND_EXIT = 212;

    @Inject
    DatabaseHelper mDatabaseHelper;

    @FragmentArg(EXTRA_TITLE)
    String mExtraTitle;

    @FragmentArg(EXTRA_TASK_ID)
    Long mExtraTaskId;

    @ViewById(android.R.id.edit)
    EditText mDescriptionEditView;

    @ViewById(R.id.tagSearchView)
    TagAutoCompleteTextView mTagAutoCompleteTextView;

    @InstanceState
    TaskBundle mTaskBundle;

    private String mTagsLoaderAttachTag;
    private String mTaskBundleLoaderAttachTag;

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cancel:
                getActivity().finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
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

        if (mExtraTaskId != null) {
            if (mTaskBundle == null) {
                requestLoad(mTaskBundleLoaderAttachTag, this);
            } else {
                bindTaskBundleToViews();
            }
        }

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
        Preconditions.checkArgument(mTaskBundle != null);

        mDescriptionEditView.setText(mTaskBundle.getTask().getDescription());
        for (Tag tag : mTaskBundle.getTags()) {
            mTagAutoCompleteTextView.addObject(tag);
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

        return Injection.sJobsComponent.loadTagListJob();
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
