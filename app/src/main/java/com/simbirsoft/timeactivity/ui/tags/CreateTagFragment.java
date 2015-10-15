package com.simbirsoft.timeactivity.ui.tags;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.base.JobEvent;
import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.db.model.Tag;
import com.simbirsoft.timeactivity.injection.Injection;
import com.simbirsoft.timeactivity.jobs.SaveTagJob;
import com.simbirsoft.timeactivity.log.LogFactory;
import com.simbirsoft.timeactivity.ui.base.AppAlertDialogFragment;
import com.simbirsoft.timeactivity.ui.base.BaseFragment;
import com.simbirsoft.timeactivity.ui.base.DialogContainerActivity;
import com.simbirsoft.timeactivity.ui.util.KeyboardUtils;
import com.simbirsoft.timeactivity.ui.util.colorpicker.ColorPickerPalette;
import com.simbirsoft.timeactivity.ui.util.colorpicker.ColorPickerSwatch;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

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

    private int mSelectedColor;

    @AfterViews
    void bindViews() {
        ActionBar mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mExtraTitle != null) {
            mActionBar.setTitle(mExtraTitle);
        }
        mPalette.init(mSize, mColumns, this);
        if (mColors != null) {
            mSelectedColor = mColors[0];
            showPaletteView();
        }
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_action_accept);
        mActionBar.setDisplayHomeAsUpEnabled(true);
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

                Tag tag = new Tag();
                tag.setName(mTagName.getText().toString());
                tag.setColor(mSelectedColor);

                SaveTagJob job = Injection.sJobsComponent.saveTagJob();
                job.setTag(tag);
                submitJob(job);

                return true;
        }
        return super.onOptionsItemSelected(item);
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
    public void onSaveTagFailed(JobEvent jobEvent) {
        switch(jobEvent.getEventCode()) {
            case SaveTagJob.EVENT_CODE_TAG_ALREADY_EXISTS:
                showSnackBarLightRed(R.string.error_tag_already_exists);
                return;
            case SaveTagJob.EVENT_CODE_TAG_NAME_IS_EMPTY:
                showSnackBarLightRed(R.string.error_tag_name_is_empty);
                return;
            default:
                LOG.debug("default error handle!");
                return;
        }
    }

    @OnJobSuccess(SaveTagJob.class)
    public void onTagSaved(SaveTagJob.SaveTagResult tag) {
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
