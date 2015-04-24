package com.simbirsoft.timemeter.ui.tags;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.SaveTagJob;
import com.simbirsoft.timemeter.ui.base.AppAlertDialogFragment;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.base.DialogContainerActivity;
import com.simbirsoft.timemeter.ui.util.KeyboardUtils;
import com.simbirsoft.timemeter.ui.util.colorpicker.ColorPickerPalette;
import com.simbirsoft.timemeter.ui.util.colorpicker.ColorPickerSwatch;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

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

    private int mSelectedColor;

    @AfterViews
    void bindViews() {
        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                KeyboardUtils.hideSoftInput(getActivity());
                if (validateInput()) {
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
        return super.onOptionsItemSelected(item);
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(mTagName.getText().toString())) {
            showSnackBarLightRed(R.string.hint_tag_name_is_empty);
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_CODE_DISCARD_CHANGES_AND_EXIT:
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
        }

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

        return true;
    }

    @Override
    public void onColorSelected(int color) {
        KeyboardUtils.hideSoftInput(getActivity());
        if (color != mSelectedColor) {
            mSelectedColor = color;
            mPalette.drawPalette(mColors, mSelectedColor);
        }
    }

    @OnJobFailure(SaveTagJob.class)
    public void onSaveTagFailed() {
        showSnackBarLightRed(R.string.error_tag_has_already_exists);
    }

    @OnJobSuccess(SaveTagJob.class)
    public void onTagSaved(SaveTagJob.SaveTagResult tag) {
        switch(tag.getEventCode()) {
            case SaveTagJob.EVENT_CODE_TAG_ALREADY_EXISTS:
                showSnackBarLightRed(R.string.error_tag_has_already_exists);
                return;
        }
        Intent data = new Intent();
        data.putExtra(EXTRA_TAG, (Parcelable) tag.getTag());
        getActivity().setResult(RESULT_CODE_OK, data);
        getActivity().finish();
    }

    public void showPaletteView() {
        if (mPalette != null) {
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
