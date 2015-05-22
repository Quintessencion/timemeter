package com.simbirsoft.timemeter.ui.taskedit;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transitions.everywhere.ChangeBounds;
import android.transitions.everywhere.Fade;
import android.transitions.everywhere.Scene;
import android.transitions.everywhere.TransitionManager;
import android.transitions.everywhere.TransitionSet;
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
import com.simbirsoft.timemeter.ui.tags.TagListAdapter;
import com.simbirsoft.timemeter.ui.util.KeyboardUtils;
import com.simbirsoft.timemeter.ui.views.TagAutoCompleteTextView;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;

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

    private TaskEditScene mTaskEditScene;
    private TaskTagsEditScene mTaskTagsEditScene;
    private String mTagsLoaderAttachTag;
    private String mTaskBundleLoaderAttachTag;
    private TagListAdapter mTagListAdapter;
    private Scene mCurrentScene;
    private ActionBar mActionBar;

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

        inflater.inflate(R.menu.fragment_edit_task, menu);
    }

    private void displayRemoveTaskAlert() {
        String title = mTaskBundle.getTask().getDescription().trim();
        if (TextUtils.isEmpty(title)) {
            title = getString(R.string.dialog_remove_task_warning_title);
        }

        Bundle args = AppAlertDialogFragment.prepareArgs(
                title,
                getString(R.string.dialog_remove_task_warning_message),
                getString(R.string.action_perform_remove),
                getString(R.string.action_cancel));

        Intent launchIntent = DialogContainerActivity.prepareDialogLaunchIntent(
                getActivity(), AppAlertDialogFragment.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, REQUEST_CODE_PERFORM_REMOVE_TASK);
    }

    private void goToEditTagsScene() {
        if (mTaskTagsEditScene != null && mCurrentScene == mTaskTagsEditScene.scene) {
            return;
        }

        mTaskTagsEditScene = createEditTagsScene();
        mCurrentScene = mTaskTagsEditScene.scene;
        bindTaskBundleToViews();

        // Load tags to the tag list
        requestLoad(mTagsLoaderAttachTag, this);

        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(new Fade(Fade.IN));
        transitionSet.addTransition(new ChangeBounds());
        transitionSet.setOrdering(TransitionSet.ORDERING_TOGETHER);
        transitionSet.setDuration(Consts.CONTENT_FADE_IN_DELAY_MILLIS);
        transitionSet.setInterpolator(new DecelerateInterpolator());
        TransitionManager.go(mTaskTagsEditScene.scene, transitionSet);
        mContentRoot.post(() -> {
            mTaskTagsEditScene.tagsView.requestFocus();
            KeyboardUtils.showSoftInput(getActivity());
        });
        mActionBar.setHomeAsUpIndicator(0);
    }

    private void goToMainScene() {
        if (mTaskEditScene != null && mCurrentScene == mTaskEditScene.scene) {
            return;
        }

        View focusView = getActivity().getCurrentFocus();
        if (focusView != null) {
            KeyboardUtils.hideSoftInput(getActivity(), focusView.getWindowToken());
        }

        if (mTaskEditScene != null) {
            mContentRoot.removeView(mTaskEditScene.layout);
        }

        mTaskEditScene = createRootScene();
        mCurrentScene = mTaskEditScene.scene;

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
        TransitionManager.go(mTaskEditScene.scene, transitionSet);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_action_accept);
    }

    @AfterViews
    void bindViews() {
        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mExtraTitle != null) {
            mActionBar.setTitle(mExtraTitle);
        }
        mActionBar.setDisplayHomeAsUpEnabled(true);
        goToMainScene();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mTaskBundle == null) {
            return super.onOptionsItemSelected(item);
        }

        switch (item.getItemId()) {
            case R.id.cancel:
                if (mTaskBundle.getTask().hasId()) {
                    displayRemoveTaskAlert();
                } else {
                    getActivity().finish();
                }

                return true;

            case android.R.id.home:
                if (mTaskTagsEditScene != null && mCurrentScene == mTaskTagsEditScene.scene) {
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

    private void bindTaskBundleToViews() {
        if (mTaskBundle == null) {
            return;
        }

        final Task task = mTaskBundle.getTask();
        if (mTaskEditScene != null && !TextUtils.isEmpty(task.getDescription())) {
            mTaskEditScene.descriptionView.setText(task.getDescription());
        }

        final List<Tag> tags = mTaskBundle.getTags();
        if (tags != null) {
            for (Tag tag : tags) {
                if (mTaskTagsEditScene != null) {
                    mTaskTagsEditScene.tagsView.addObject(tag);
                }
                if (mTaskEditScene != null) {
                    mTaskEditScene.tagsView.addObject(tag);
                }
            }
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
    public void onTokenAdded(Object o) {
        Tag tag = (Tag) o;
        List<Tag> bundledTags = mTaskBundle.getTags();

        if (tag.getId() == null) {
            tag.setName(tag.getName().trim());
            Tag item = mTagListAdapter.findItemWithName(tag.getName());
            if (item != null) {
                tag = item;
            }
        }

        if (bundledTags.contains(tag)) {
            final Tag addedToken = tag;
            List<Object> tokens = mTaskTagsEditScene.tagsView.getObjects();
            int count = Collections2.filter(tokens, (token) ->
                    ((Tag) token).getName().equalsIgnoreCase(addedToken.getName())).size();

            if (count > 1) {
                mTaskTagsEditScene.tagsView.removeObject(o);
            }
        } else {
            bundledTags.add(tag);
            if (!getActivity().isFinishing()) {
                refilterTagList();
            }
        }
    }

    @Override
    public void onTokenRemoved(Object o) {
        mTaskBundle.getTags().remove((Tag) o);
    }

    private void refilterTagList() {
        if (mTagListAdapter != null) {
            mTagListAdapter.getFilter()
                    .clearExclusions()
                    .excludeTags(mTaskBundle.getTags())
                    .filter(mTagFilter);
        }
    }

    private TaskEditScene createRootScene() {
        TaskEditScene scene = TaskEditScene.create(getActivity(), mContentRoot);
        scene.tagsView.allowDuplicates(false);
        scene.tagsView.allowCollapse(false);
        scene.tagsView.setImeActionLabel(
                getString(R.string.ime_action_create),
                EditorInfo.IME_ACTION_NEXT);
        scene.tagsView.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        scene.tagsView.setOnFocusChangeListener((view, isFocused) -> {
            if (isFocused) {
                goToEditTagsScene();
            }
        });
        scene.tagsView.setAdapter(createTokenAdapterStub());
        scene.descriptionView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                mTaskBundle.getTask().setDescription(scene.descriptionView.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        return scene;
    }

    private TaskTagsEditScene createEditTagsScene() {
        TaskTagsEditScene scene = TaskTagsEditScene.create(getActivity(), mContentRoot);
        scene.tagsView.setTokenListener(this);
        scene.tagsView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                String text = charSequence.toString();
                int pos = text.lastIndexOf(", ");
                if (pos > -1) {
                    pos += 2; // Skip dot-space
                } else {
                    pos = 0; // Consider full text
                }

                if (pos >= text.length()) {
                    if (!TextUtils.isEmpty(mTagFilter)) {
                        mTaskTagsEditScene.hideCreateTagView();
                    }
                    mTagFilter = null;
                } else {
                    String input = text.substring(pos).trim();
                    if (!Objects.equal(input, mTagFilter)) {
                        if (TextUtils.isEmpty(input)
                                || input.length() < 2
                                || mTagListAdapter.containsItemWithName(input)) {

                            mTaskTagsEditScene.hideCreateTagView();
                        } else {
                            mTaskTagsEditScene.showCreateTagView(input);
                        }
                    }
                    mTagFilter = input;
                }
                refilterTagList();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        scene.tagsView.allowDuplicates(false);
        scene.tagsView.setImeActionLabel(
                getString(R.string.ime_action_done),
                EditorInfo.IME_ACTION_DONE);
        scene.tagsView.setAdapter(createTokenAdapterStub());
        RecyclerView.LayoutManager mTagListLayoutManager = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false);
        scene.tagsRecyclerView.setLayoutManager(mTagListLayoutManager);
        scene.tagsRecyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getActivity()).build());
        scene.createTagView.setOnClickListener((v) -> onCreateTagClicked());

        mTagListAdapter = new TagListAdapter();
        mTagListAdapter.setItemClickListener(new TagListAdapter.AbsItemClickListener() {
            @Override
            public void onItemClicked(Tag item) {
                if (!TextUtils.isEmpty(mTagFilter)) {
                    scene.tagsView.performCompletion();
                    mContentRoot.post(() -> {
                        List<Object> tags = scene.tagsView.getObjects();
                        if (!tags.isEmpty()) {
                            scene.tagsView.removeObject(tags.get(tags.size() - 1));
                        }
                    });
                }
                mContentRoot.post(() -> {
                    mTaskBundle.getTags().add(item);
                    scene.tagsView.addObject(item);
                    refilterTagList();
                });
            }
        });
        scene.tagsRecyclerView.setAdapter(mTagListAdapter);

        return scene;
    }

    private void onCreateTagClicked() {
        if (TextUtils.isEmpty(mTagFilter)) {
            return;
        }

        mTaskTagsEditScene.tagsView.performCompletion();
    }

    private ArrayAdapter<Tag> createTokenAdapterStub() {
        return new FilteredArrayAdapter<Tag>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                new Tag[0]) {

            @Override
            protected boolean keepObject(Tag tag, String s) {
                final String name = s.trim();

                if (TextUtils.isEmpty(name)) {
                    return false;
                }

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
    }
}
