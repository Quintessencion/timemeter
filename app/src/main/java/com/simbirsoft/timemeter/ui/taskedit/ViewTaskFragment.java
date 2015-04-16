package com.simbirsoft.timemeter.ui.taskedit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.base.FragmentContainerActivity;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.util.TagViewUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.apmem.tools.layouts.FlowLayout;
import org.slf4j.Logger;

import java.util.List;

@EFragment(R.layout.fragment_view_task)
public class ViewTaskFragment extends BaseFragment {
    public static final String EXTRA_TASK_BUNDLE = "extra_task_bundle";

    private static final Logger LOG = LogFactory.getLogger(ViewTaskFragment.class);

    private static final int REQUEST_CODE_EDIT_TASK = 100;

    @FragmentArg(EXTRA_TASK_BUNDLE)
    TaskBundle mExtraTaskBundle;

    @ViewById(R.id.tagViewContainer)
    FlowLayout tagViewContainer;

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
}