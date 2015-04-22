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
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.injection.Injection;
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

/**
 * Created by Alexander Ismailov on 20.04.15.
 */
@EFragment(R.layout.fragment_tag_create)
public class CreateTagFragment extends BaseFragment implements ColorPickerSwatch.OnColorSelectedListener {

    public static final int RESULT_CODE_OK = Activity.RESULT_OK;

    public static final int SIZE_LARGE = 1;
    public static final int SIZE_SMALL = 2;

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_TAG = "extra_tag";

    protected static final String KEY_COLORS = "colors";
    protected static final String KEY_COLUMNS = "columns";
    protected static final String KEY_SIZE = "size";

    private static final int REQUEST_CODE_DISCARD_CHANGES_AND_EXIT = 212;

    private static final String SNACKBAR_TAG = "tag_list_snackbar";

    private static final Logger LOG = LogFactory.getLogger(CreateTagFragment.class);

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

    @AfterViews
    void bindViews() {
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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        LOG.debug("destroy");
        Snackbar sb = SnackbarManager.getCurrentSnackbar();
        if (sb != null && sb.isShowing() && SNACKBAR_TAG.equals(sb.getTag())) {
            sb.dismiss();
        }
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
                    tag.setId(null);

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
            Snackbar bar = Snackbar.with(getActivity())
                    .type(SnackbarType.MULTI_LINE)
                    .text(R.string.hint_tag_name_is_empty)
                    .colorResource(R.color.lightRed)
                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE);
            bar.setTag(SNACKBAR_TAG);
            SnackbarManager.show(bar);
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
        if (TextUtils.isEmpty(mTagName.getText().toString())) {
            return super.handleBackPress();
        } else {
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
        if (getTargetFragment() instanceof ColorPickerSwatch.OnColorSelectedListener) {
            final ColorPickerSwatch.OnColorSelectedListener listener =
                    (ColorPickerSwatch.OnColorSelectedListener) getTargetFragment();
            listener.onColorSelected(color);
        }

        if (color != mSelectedColor) {
            mSelectedColor = color;
            mPalette.drawPalette(mColors, mSelectedColor);
        }
    }

    @OnJobFailure(SaveTagJob.class)
    public void onSaveTagFailed() {
        Snackbar bar = Snackbar.with(getActivity())
                .type(SnackbarType.MULTI_LINE)
                .text(R.string.error_unable_to_save_tag)
                .colorResource(R.color.lightRed)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE);
        bar.setTag(SNACKBAR_TAG);
        SnackbarManager.show(bar);
    }

    @OnJobSuccess(SaveTagJob.class)
    public void onTagSaved(SaveTagJob.SaveTagResult tag) {
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

    private void refreshPalette() {
        if (mPalette != null && mColors != null) {
            mPalette.drawPalette(mColors, mSelectedColor);
        }
    }
}
