package com.simbirsoft.timemeter.ui.taskedit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.transitions.everywhere.ChangeBounds;
import android.transitions.everywhere.Fade;
import android.transitions.everywhere.TransitionManager;
import android.transitions.everywhere.TransitionSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.Consts;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.jobs.LoadTaskBundleJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.base.FragmentContainerActivity;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.util.TagViewUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;

@EFragment(R.layout.fragment_view_task)
public class ViewTaskFragment extends BaseFragment {
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TASK_BUNDLE = "extra_task_bundle";

    private static final Logger LOG = LogFactory.getLogger(ViewTaskFragment.class);

    private static final int REQUEST_CODE_EDIT_TASK = 100;

    @FragmentArg(EXTRA_TITLE)
    String mExtraTitle;

    @FragmentArg(EXTRA_TASK_ID)
    Long mExtraTaskId;

    @FragmentArg(EXTRA_TASK_BUNDLE)
    TaskBundle mExtraTaskBundle;

    @ViewById(R.id.rootScene)
    ViewGroup mContentRoot;

    private TaskViewScene mTaskViewScene;
    private ActionBar mActionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        setShouldSubscribeForJobEvents(false);
    }

//    @OnJobSuccess(LoadTaskBundleJob.class)
//    public void onTaskBundleLoaded(LoadJobResult<TaskBundle> taskBundle) {}

    // Без обьявления данного метода почемуто падает проект во время открытия просмотра задач
    @OnJobFailure(LoadTaskBundleJob.class)
    public void onTaskBundleLoadFailure() {}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_view_task, menu);
    }

    public void bindTagViews(ViewGroup tagLayout, List<Tag> tags) {
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

    private void bindTaskBundleToViews() {
        if (mExtraTaskBundle == null) {
            return;
        }

        bindTagViews(mTaskViewScene.tagViewContainer, mExtraTaskBundle.getTags());
    }

    private TaskViewScene createRootScene() {
        return TaskViewScene.create(getActivity(), mContentRoot);
    }

    private void setActionBarTitle(String title) {
        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (title != null) {
            mActionBar.setTitle(title);
        }
    }

    private void goToMainScene() {
        mTaskViewScene = createRootScene();

        bindTaskBundleToViews();

        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(new Fade(Fade.IN));
        transitionSet.addTransition(new ChangeBounds());
        transitionSet.setOrdering(TransitionSet.ORDERING_TOGETHER);
        transitionSet.setDuration(Consts.CONTENT_FADE_IN_DELAY_MILLIS);
        transitionSet.setInterpolator(new DecelerateInterpolator());
        TransitionManager.go(mTaskViewScene.scene, transitionSet);

        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @AfterViews
    void bindViews() {
        setActionBarTitle(mExtraTitle);
        goToMainScene();
    }

    private void goToEditTask() {
        Bundle args = new Bundle();
        args.putString(EditTaskFragment.EXTRA_TITLE, getString(R.string.title_edit_task));
        args.putLong(EditTaskFragment.EXTRA_TASK_ID, mExtraTaskId);

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
    public boolean handleBackPress() {
        getActivity().finish();
        return true;
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
                        bindTagViews(mTaskViewScene.tagViewContainer, bundle.getTags());
                        setActionBarTitle(bundle.getTask().getDescription());
                        break;
                }
                return;

            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}