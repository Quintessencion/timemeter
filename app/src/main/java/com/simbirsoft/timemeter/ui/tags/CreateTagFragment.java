package com.simbirsoft.timemeter.ui.tags;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTagListJob;
import com.simbirsoft.timemeter.jobs.SaveTagJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.AppAlertDialogFragment;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.base.DialogContainerActivity;
import com.simbirsoft.timemeter.ui.util.colorpicker.ColorPickerPalette;
import com.simbirsoft.timemeter.ui.util.colorpicker.ColorPickerSwatch;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;

/**
 * Created by Alexander Ismailov on 20.04.15.
 */
@EFragment(R.layout.fragment_tag_create)
public class CreateTagFragment extends BaseFragment implements ColorPickerSwatch.OnColorSelectedListener, JobLoader.JobLoaderCallbacks {

    public static final int RESULT_CODE_OK = Activity.RESULT_OK;

    public static final int SIZE_LARGE = 1;
    public static final int SIZE_SMALL = 2;

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_TAG = "extra_tag";

    protected static final String KEY_COLORS = "colors";
    protected static final String KEY_COLUMNS = "columns";
    protected static final String KEY_SIZE = "size";

    private static final Logger LOG = LogFactory.getLogger(CreateTagFragment.class);
    private static final int REQUEST_CODE_DISCARD_CHANGES_AND_EXIT = 212;

    private final String mLoaderAttachTag = getClass().getName() + "_loader";
    private static final String SNACKBAR_TAG = "tag_list_snackbar";

    @FragmentArg(EXTRA_TITLE)
    String mExtraTitle;

    @FragmentArg(KEY_COLORS)
    int[] mColors;

    @FragmentArg(KEY_COLUMNS)
    int mColumns;

    @FragmentArg(KEY_SIZE)
    int mSize;

    @ViewById(R.id.edit)
    EditText mTagName;

    @ViewById(R.id.color_picker)
    ColorPickerPalette mPalette;

    @ViewById(R.id.progress)
    ProgressBar mProgress;

    private int mSelectedColor;
    private ActionBar mActionBar;
    private List<Tag> mTagList = null;

    @AfterViews
    void bindViews() {
        LOG.debug("bind views");
        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (mExtraTitle != null) {
            mActionBar.setTitle(mExtraTitle);
        }
        mPalette.init(mSize, mColumns, this);
        if (mColors != null) {
            mSelectedColor = mColors[0];
            showPaletteView();
        }
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_action_accept);

        LOG.debug("request load tag list");
        requestLoad(mLoaderAttachTag, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOG.debug("create");
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        LOG.debug("destroy");
        super.onDestroy();
    }

    @Override
    public void onPause() {
        LOG.debug("pause");
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LOG.debug("Options item selected:");
        switch(item.getItemId()) {
            case android.R.id.home:
                LOG.debug("selected home");
                if (validateInput()) {
                    LOG.debug("tag name isn't empty -> create tag");
                    Tag tag = new Tag();
                    tag.setName(mTagName.getText().toString());
                    tag.setColor(mSelectedColor);

                    SaveTagJob job = Injection.sJobsComponent.saveTagJob();
                    job.setTag(tag);
                    submitJob(job);
                }
                return true;
        }
        LOG.debug("default");
        return super.onOptionsItemSelected(item);
    }

    private boolean validateInput() {
        LOG.debug("Check input:");
        if (TextUtils.isEmpty(mTagName.getText().toString())) {
            LOG.debug("tag name is empty -> show snackbar");
            SnackbarManager.show(Snackbar.with(getActivity())
                    .text(R.string.hint_task_description_is_empty)
                    .colorResource(R.color.lightRed)
                    .animation(false)
                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE));

            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LOG.debug("Result activity:");
        switch(requestCode) {
            case REQUEST_CODE_DISCARD_CHANGES_AND_EXIT:
                LOG.debug("with code: REQUEST_CODE_DISCARD_CHANGES_AND_EXIT");
                if (resultCode == AppAlertDialogFragment.RESULT_CODE_ACCEPTED) {
                    getActivity().finish();
                    return;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean handleBackPress() {
        LOG.debug("Back pressed");
        if (TextUtils.isEmpty(mTagName.getText().toString())) {
            LOG.debug("Tag name is empty -> quit");
            return super.handleBackPress();
        } else {
            LOG.debug("Tag name isn't empty ->show alert dialog");
            Bundle args = AppAlertDialogFragment.prepareArgs(getActivity(),
                    R.string.dialog_cancel_tag_changes_warning_title,
                    R.string.dialog_cancel_tag_changes_warning_message,
                    R.string.action_accept_yes,
                    R.string.action_cancel);

            Intent launchIntent = DialogContainerActivity.prepareDialogLaunchIntent(
                    getActivity(),
                    AppAlertDialogFragment.class.getName(),
                    args);

            getActivity().startActivityForResult(launchIntent,
                    REQUEST_CODE_DISCARD_CHANGES_AND_EXIT);
        }
        return true;
    }

    @Override
    public void onColorSelected(int color) {
        LOG.debug("Color selected");
        if (getTargetFragment() instanceof ColorPickerSwatch.OnColorSelectedListener) {
            final ColorPickerSwatch.OnColorSelectedListener listener =
                    (ColorPickerSwatch.OnColorSelectedListener) getTargetFragment();
            listener.onColorSelected(color);
        }

        if (color != mSelectedColor) {
            mSelectedColor = color;
            // Redraw palette to show checkmark on newly selected color before dismissing.
            mPalette.drawPalette(mColors, mSelectedColor);
        }
    }

    @Override
    public Job onCreateJob(String tag) {
        LOG.debug("Create job");
        return Injection.sJobsComponent.loadTagListJob();
    }

    @OnJobSuccess(LoadTagListJob.class)
    public void onTagListLoaded(LoadJobResult<List<Tag>> result) {
        LOG.debug("Tag list loaded successfull");
        mTagList = result.getData();
    }

    @OnJobFailure(LoadTagListJob.class)
    public void onTagListLoadFailed() {
        LOG.debug("Tag list load failed");
        Snackbar bar = Snackbar.with(getActivity())
                .type(SnackbarType.MULTI_LINE)
                .text(R.string.error_unable_to_load_tag_list)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                .color(getResources().getColor(R.color.lightRed));
        bar.setTag(SNACKBAR_TAG);
        SnackbarManager.show(bar);
    }

    @OnJobFailure(SaveTagJob.class)
    public void onSaveTagFailed() {
        LOG.debug("Tag not created");
        requestLoad(mLoaderAttachTag, this);
        Snackbar bar = Snackbar.with(getActivity())
                .text(R.string.error_unable_to_save_tag)
                .colorResource(R.color.lightRed)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE);
        bar.setTag(SNACKBAR_TAG);
        SnackbarManager.show(bar);
    }

    @OnJobSuccess(SaveTagJob.class)
    public void onTagSaved(SaveTagJob.SaveTagResult tag) {
        LOG.debug("Tag created and saved");

        Intent data = new Intent();
        data.putExtra(EXTRA_TAG, (Parcelable) tag.getTag());

        getActivity().setResult(RESULT_CODE_OK, data);
        getActivity().finish();
    }

    public void showPaletteView() {
        if (mProgress != null && mPalette != null) {
            mProgress.setVisibility(View.GONE);
            refreshPalette();
            mPalette.setVisibility(View.VISIBLE);
        }
    }

    public void showProgressBarView() {
        if (mProgress != null && mPalette != null) {
            mProgress.setVisibility(View.VISIBLE);
            mPalette.setVisibility(View.GONE);
        }
    }

    public void setColors(int[] colors, int selectedColor) {
        if (mColors != colors || mSelectedColor != selectedColor) {
            mColors = colors;
            mSelectedColor = selectedColor;
            refreshPalette();
        }
    }

    public void setColors(int[] colors) {
        if (mColors != colors) {
            mColors = colors;
            refreshPalette();
        }
    }

    public void setSelectedColor(int color) {
        if (mSelectedColor != color) {
            mSelectedColor = color;
            refreshPalette();
        }
    }

    private void refreshPalette() {
        if (mPalette != null && mColors != null) {
            mPalette.drawPalette(mColors, mSelectedColor);
        }
    }
}
