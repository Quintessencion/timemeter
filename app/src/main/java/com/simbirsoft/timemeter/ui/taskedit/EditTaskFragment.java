package com.simbirsoft.timemeter.ui.taskedit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.DatabaseHelper;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.ui.base.AppAlertDialogFragment;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.base.DialogContainerActivity;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import javax.inject.Inject;

@EFragment(R.layout.fragment_edit_task)
public class EditTaskFragment extends BaseFragment {

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

    @InstanceState
    boolean mIsChanged = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setShouldSubscribeForJobEvents(false);

        super.onCreate(savedInstanceState);

        Injection.sUiComponent.injectEditTaskFragment(this);

        setHasOptionsMenu(true);
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

        Task task = loadTask();
        if (task != null) {
            mDescriptionEditView.setText(task.getDescription());
        }
    }

    private Task loadTask() {
        if (mExtraTaskId == null) {
            return null;
        }

        return cupboard().withDatabase(mDatabaseHelper.getWritableDatabase())
                .query(Task.class)
                .byId(mExtraTaskId)
                .get();
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
        if (!mIsChanged) {
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
}
