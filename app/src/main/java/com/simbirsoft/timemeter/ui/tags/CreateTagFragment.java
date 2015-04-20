package com.simbirsoft.timemeter.ui.tags;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.simbirsoft.timemeter.R;
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

    public static final int SIZE_LARGE = 1;
    public static final int SIZE_SMALL = 2;

    public static final String EXTRA_TITLE = "extra_title";

    protected static final String KEY_COLORS = "colors";
    protected static final String KEY_COLUMNS = "columns";
    protected static final String KEY_SIZE = "size";


    private static final Logger LOG = LogFactory.getLogger(CreateTagFragment.class);
    private static final int REQUEST_CODE_DISCARD_CHANGES_AND_EXIT = 212;

    @FragmentArg(EXTRA_TITLE)
    String mExtraTitle;

    @FragmentArg(KEY_COLORS)
    int[] mColors;

    @FragmentArg(KEY_COLUMNS)
    int mColumns;

    @FragmentArg(KEY_SIZE)
    int mSize;

    @ViewById(R.id.color_picker)
    ColorPickerPalette mPalette;

    @ViewById(R.id.progress)
    ProgressBar mProgress;

    protected int mSelectedColor;

    protected ColorPickerSwatch.OnColorSelectedListener mListener;

    private ActionBar mActionBar;

    public void initialize(int[] colors, int columns, int size) {
        setColors(colors, 0);
    }

//    public void setOnColorSelectedListener(ColorPickerSwatch.OnColorSelectedListener listener) {
//        mListener = listener;
//    }

    @AfterViews
    void bindViews() {
        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (mExtraTitle != null) {
            mActionBar.setTitle(mExtraTitle);
        }
        mPalette.init(mSize, mColumns, this);
        if (mColors != null) {
            showPaletteView();
        }
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_action_accept);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setShouldSubscribeForJobEvents(false);
        super.onCreate(savedInstanceState);

        setSelectedColor(0);
        //if (savedInstanceState != null) {
        //    mColors = savedInstanceState.getIntArray(KEY_COLORS);
        //}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.home:
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
        if (/*(mTaskBundle == null) || mTaskBundle.isEqualToSavedState()*/false) {
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
            // Redraw palette to show checkmark on newly selected color before dismissing.
            mPalette.drawPalette(mColors, mSelectedColor);
        }
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
