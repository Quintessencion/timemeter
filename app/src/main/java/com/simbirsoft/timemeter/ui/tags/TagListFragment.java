package com.simbirsoft.timemeter.ui.tags;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.transitions.everywhere.Fade;
import android.transitions.everywhere.TransitionManager;
import android.transitions.everywhere.Visibility;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.EventListener;
import com.simbirsoft.timemeter.Consts;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.BackupTagJob;
import com.simbirsoft.timemeter.jobs.LoadTagListJob;
import com.simbirsoft.timemeter.jobs.RemoveTagJob;
import com.simbirsoft.timemeter.jobs.SaveTagJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseActivity;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.base.DialogContainerActivity;
import com.simbirsoft.timemeter.ui.base.FragmentContainerActivity;
import com.simbirsoft.timemeter.ui.main.TaskListFragment;
import com.simbirsoft.timemeter.ui.model.TagBundle;
import com.simbirsoft.timemeter.ui.taskedit.EditTaskFragment;
import com.simbirsoft.timemeter.ui.taskedit.EditTaskFragment_;
import com.simbirsoft.timemeter.ui.util.colorpicker.ColorPickerDialog;
import com.simbirsoft.timemeter.ui.util.colorpicker.ColorPickerSwatch;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;

@EFragment(R.layout.fragment_tag_list)
public class TagListFragment extends BaseFragment implements JobLoader.JobLoaderCallbacks,
        TagListAdapter.ItemClickListener,
        ColorPickerSwatch.OnColorSelectedListener {


    private static final Logger LOG = LogFactory.getLogger(TaskListFragment.class);

    private static final int REQUEST_CODE_EDIT_TAG_NAME = 10002;
    private static final String SNACKBAR_TAG = "tag_list_snackbar";
    private static final int DEFAULT_COLOR_COLUMNS_COUNT = 4;
    private static final String TAG_COLOR_PICKER = "color_picker";

    @ViewById(android.R.id.list)
    RecyclerView mRecyclerView;

    @InstanceState
    boolean mIsInActionMode;

    @InstanceState
    long mEditTagId = -1;

    @InstanceState
    Integer mTagListPosition;

    private TagListAdapter mTagListAdapter;
    private Toolbar mToolbar;
    private final String mLoaderAttachTag = getClass().getName() + "_loader";
    private ActionMode mActionMode;
    private final ActionMode.Callback mActionModeCallbacks =
            new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    mIsInActionMode = true;
                    TransitionManager.beginDelayedTransition(mToolbar, new Fade(Visibility.MODE_OUT));
                    mToolbar.setVisibility(View.INVISIBLE);
                    mTagListAdapter.setActionButtonsShown(true);

                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode actionMode) {
                    TransitionManager.beginDelayedTransition(mToolbar, new Fade(Fade.MODE_IN));
                    mToolbar.setVisibility(View.VISIBLE);
                    mTagListAdapter.setActionButtonsShown(false);
                    mIsInActionMode = false;
                    mActionMode = null;
                }
            };

    @AfterViews
    void bindViews() {
        mToolbar = ((BaseActivity) getActivity()).getToolbar();
        RecyclerView.LayoutManager mTagListLayoutManager = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false);
        mRecyclerView.setLayoutManager(mTagListLayoutManager);
        mRecyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getActivity()).build());

        mTagListAdapter = new TagListAdapter();
        mTagListAdapter.setItemClickListener(this);
        mRecyclerView.setAdapter(mTagListAdapter);

        if (mIsInActionMode) {
            startActionMode();
        }

        requestLoad(mLoaderAttachTag, this);

        ColorPickerDialog colorPicker = findColorPickerDialog();
        if (colorPicker != null) {
            colorPicker.setOnColorSelectedListener(this);
        }
    }

    private ColorPickerDialog findColorPickerDialog() {
        return (ColorPickerDialog) getChildFragmentManager().findFragmentByTag(TAG_COLOR_PICKER);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        if (mTagListAdapter != null) {
            mTagListAdapter.setItemClickListener(null);
        }
        ColorPickerDialog colorPicker = findColorPickerDialog();
        if (colorPicker != null) {
            colorPicker.setOnColorSelectedListener(null);
        }

        Snackbar sb = SnackbarManager.getCurrentSnackbar();
        if (sb != null && sb.isShowing() && SNACKBAR_TAG.equals(sb.getTag())) {
            sb.dismiss();
        }

        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mTagListAdapter.getItemCount() > 0) {
            mTagListPosition = ((LinearLayoutManager)
                    mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_tag_list, menu);
    }

    private void startActionMode() {
        mActionMode = getActivity().startActionMode(mActionModeCallbacks);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                startActionMode();
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Job onCreateJob(String tag) {
        return Injection.sJobsComponent.loadTagListJob();
    }

    @OnJobSuccess(LoadTagListJob.class)
    public void onTagListLoaded(LoadJobResult<List<Tag>> result) {
        mTagListAdapter.setItems(result.getData());

        if (mTagListPosition != null) {
            mRecyclerView.getLayoutManager().scrollToPosition(mTagListPosition);
            mTagListPosition = null;
        }
    }

    @OnJobFailure(LoadTagListJob.class)
    public void onTagListLoadFailed() {
        Snackbar bar = Snackbar.with(getActivity())
                .type(SnackbarType.MULTI_LINE)
                .text(R.string.error_unable_to_load_tag_list)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                .color(getResources().getColor(R.color.lightRed));
        bar.setTag(SNACKBAR_TAG);
        SnackbarManager.show(bar);
    }

    @Override
    public void onItemEditClicked(Tag item) {
        Bundle args = new Bundle();
        args.putParcelable(EditTagNameDialogFragment.EXTRA_TAG, item);
        Intent launchIntent = DialogContainerActivity.prepareDialogLaunchIntent(
                getActivity(), EditTagNameDialogFragment.class.getName(), args);
        startActivityForResult(launchIntent, REQUEST_CODE_EDIT_TAG_NAME);
    }

    @Override
    public void onItemEditLongClicked(Tag item, View itemView) {
        showToastWithAnchor(getString(R.string.hint_edit_tag_name), itemView);
    }

    @Override
    public void onItemEditColorClicked(Tag item) {
        mEditTagId = item.getId();
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        int[] colors = getResources().getIntArray(R.array.default_tag_colors);
        int size = isTablet ? ColorPickerDialog.SIZE_LARGE : ColorPickerDialog.SIZE_SMALL;

        ColorPickerDialog dialog = ColorPickerDialog.newInstance(
                R.string.dialog_edit_tag_color_title,
                colors,
                item.getColor(),
                DEFAULT_COLOR_COLUMNS_COUNT,
                size);

        dialog.show(getChildFragmentManager(), TAG_COLOR_PICKER);
        dialog.setOnColorSelectedListener(this);
    }

    @Override
    public void onItemEditColorLongClicked(Tag item, View itemView) {
        showToastWithAnchor(getString(R.string.hint_edit_tag_color), itemView);
    }

    @Override
    public void onItemClicked(Tag item) {
    }

    @Override
    public void onItemRemoveLongClicked(Tag item, View itemView) {
        showToastWithAnchor(getString(R.string.hint_remove), itemView);
    }

    @Override
    public void onItemRemoveClicked(Tag item) {
        mTagListAdapter.removeItem(item);
        RemoveTagJob job = Injection.sJobsComponent.removeTagJob();
        job.setTagId(item.getId());
        submitJob(job);
    }

    private void backupRemovedTag(Snackbar snackbar, TagBundle tagBundle) {
        snackbar.dismiss();

        mRecyclerView.postDelayed(() -> {
            BackupTagJob job = Injection.sJobsComponent.backupTagJob();
            job.setTagBundle(tagBundle);
            submitJob(job);
        }, Consts.DISMISS_DELAY_MILLIS);
    }

    @OnJobSuccess(RemoveTagJob.class)
    public void onTagRemoved(LoadJobResult<TagBundle> result) {
        final TagBundle tagBundle = result.getData();
        final String tagName = tagBundle.getTag().getName();
        final String undoMessage = getString(R.string.hint_tag_removed)
                + "\n"
                + tagName;

        final SpannableStringBuilder sb = new SpannableStringBuilder(undoMessage);
        final StyleSpan iss = new StyleSpan(Typeface.ITALIC);
        sb.setSpan(iss, undoMessage.length() - tagName.length(),
                undoMessage.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        Snackbar bar = Snackbar.with(getActivity())
                .type(SnackbarType.MULTI_LINE)
                .text(sb)
                .actionLabel(R.string.action_undo_remove)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                .attachToRecyclerView(mRecyclerView)
                .color(getResources().getColor(R.color.primaryDark))
                .actionListener((snackbar) -> backupRemovedTag(snackbar, tagBundle));
        bar.setTag(SNACKBAR_TAG);
        SnackbarManager.show(bar);
    }

    @OnJobFailure(RemoveTagJob.class)
    public void onTagRemoveFailed() {
        requestLoad(mLoaderAttachTag, this);
        Snackbar bar = Snackbar.with(getActivity())
                .text(R.string.error_unable_to_remove_tag)
                .colorResource(R.color.lightRed)
                .attachToRecyclerView(mRecyclerView)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE);
        bar.setTag(SNACKBAR_TAG);
        SnackbarManager.show(bar);
    }

    @OnJobSuccess(BackupTagJob.class)
    public void onTagBackedUp() {
        requestLoad(mLoaderAttachTag, this);
    }

    @OnJobFailure(BackupTagJob.class)
    public void onTagBackupFailed() {
        requestLoad(mLoaderAttachTag, this);
        Snackbar bar = Snackbar.with(getActivity())
                .text(R.string.error_unable_to_backup_tag)
                .colorResource(R.color.lightRed)
                .attachToRecyclerView(mRecyclerView)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE);
        bar.setTag(SNACKBAR_TAG);
        SnackbarManager.show(bar);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_EDIT_TAG_NAME:
                if (resultCode == DialogContainerActivity.RESULT_OK) {
                    Tag tag = data.getParcelableExtra(EditTagNameDialogFragment.EXTRA_TAG);
                    mTagListAdapter.replaceItem(mRecyclerView, tag);
                }

                return;

            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean handleBackPress() {
        if (mActionMode != null) {
            mActionMode.finish();

            return true;
        }

        return super.handleBackPress();
    }

    @Override
    public void onColorSelected(int color) {
        if (mEditTagId == -1) return;

        Tag item = mTagListAdapter.findItemById(mEditTagId);
        mEditTagId = -1;

        if (item == null) {
            return;
        }

        item.setColor(color);
        mTagListAdapter.replaceItem(mRecyclerView, item);
        saveTag(item);
    }

    @OnJobFailure(SaveTagJob.class)
    public void onSaveTagFailed() {
        requestLoad(mLoaderAttachTag, this);
        Snackbar bar = Snackbar.with(getActivity())
                .text(R.string.error_unable_to_save_tag)
                .colorResource(R.color.lightRed)
                .attachToRecyclerView(mRecyclerView)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE);
        bar.setTag(SNACKBAR_TAG);
        SnackbarManager.show(bar);
    }

    private void saveTag(Tag tag) {
        SaveTagJob saveTagJob = Injection.sJobsComponent.saveTagJob();
        saveTagJob.setTag(tag);
        submitJob(saveTagJob);
    }
}
