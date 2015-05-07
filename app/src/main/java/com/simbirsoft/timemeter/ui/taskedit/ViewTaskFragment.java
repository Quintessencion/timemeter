package com.simbirsoft.timemeter.ui.taskedit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.base.FragmentContainerActivity;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.views.TagFlowView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

@EFragment(R.layout.fragment_view_task)
public class ViewTaskFragment extends BaseFragment {
    public static final String EXTRA_TASK_BUNDLE = "extra_task_bundle";

    private static final Logger LOG = LogFactory.getLogger(ViewTaskFragment.class);

    private static final int REQUEST_CODE_EDIT_TASK = 100;

    @ViewById(R.id.tagFlowView)
    protected TagFlowView tagFlowView;

    @FragmentArg(EXTRA_TASK_BUNDLE)
    TaskBundle mExtraTaskBundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setShouldSubscribeForJobEvents(false);
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_view_task, menu);
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
        tagFlowView.bindTagViews(mExtraTaskBundle.getTags());
    }

    private void goToEditTask() {
        Bundle args = new Bundle();
        args.putString(EditTaskFragment.EXTRA_TITLE, getString(R.string.title_edit_task));
        args.putLong(EditTaskFragment.EXTRA_TASK_ID, mExtraTaskBundle.getTask().getId());

        Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                getActivity(), EditTaskFragment_.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, REQUEST_CODE_EDIT_TASK);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                LOG.debug("task edit clicked");
                goToEditTask();
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
                        tagFlowView.bindTagViews(bundle.getTags());
                        setActionBarTitleAndHome(bundle.getTask().getDescription());
                        break;
                }
                return;

            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}