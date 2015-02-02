package com.simbirsoft.timemeter.ui.tags;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.google.common.base.Preconditions;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.SaveTagJob;
import com.simbirsoft.timemeter.ui.base.BaseDialogFragment;
import com.simbirsoft.timemeter.ui.base.FragmentContainerCallbacks;

public class EditTagNameDialogFragment extends BaseDialogFragment {

    public static final String EXTRA_TAG = "extra_tag";

    private static final String STATE_ENTERED_TEXT = "entered_text";

    private EditText mEditNameView;
    private Tag mTag;
    private View mPositiveButton;
    private View mNegativeButton;
    private String mEnteredText;
    private FragmentContainerCallbacks mContainerCallbacks;
    private MaterialDialog mDialog;
    private final String mSaveJobTag = "save_tag_name";

    private final TextView.OnEditorActionListener mOnEditorActionListener =
            (textView, actionId, keyEvent) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Toast.makeText(getActivity(), "DONE", Toast.LENGTH_SHORT).show();
                    return true;
                }

                return false;
            };

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            mEnteredText = charSequence.toString().trim();
            mPositiveButton.setEnabled(!TextUtils.isEmpty(mEnteredText));
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof FragmentContainerCallbacks) {
            mContainerCallbacks = (FragmentContainerCallbacks) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTag = getArguments().getParcelable(EXTRA_TAG);
        Preconditions.checkArgument(mTag != null);

        if (savedInstanceState != null) {
            mEnteredText = savedInstanceState.getString(STATE_ENTERED_TEXT);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_ENTERED_TEXT, mEnteredText);
    }

    @OnJobSuccess(SaveTagJob.class)
    public void onTagSaved(SaveTagJob.SaveTagResult result) {
        Intent data = new Intent();
        data.putExtra(EXTRA_TAG, (Parcelable) mTag);

        mDialog.dismiss();

        if (mContainerCallbacks != null) {
            mContainerCallbacks.setResult(Activity.RESULT_OK, data);
            mContainerCallbacks.finish();
        }
    }

    @OnJobFailure(SaveTagJob.class)
    public void onTagSaveFailed() {
        enableViews();
    }

    private void disableViews() {
        mPositiveButton.setEnabled(false);
        mNegativeButton.setEnabled(false);
        mEditNameView.setEnabled(false);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
    }

    private void enableViews() {
        mPositiveButton.setEnabled(true);
        mNegativeButton.setEnabled(true);
        mEditNameView.setEnabled(true);
        mDialog.setCancelable(true);
        mDialog.setCanceledOnTouchOutside(true);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        if (mContainerCallbacks != null) {
            mContainerCallbacks.finish();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View root = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_edit_tag_name, null);

        mDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_edit_tag_name_title)
                .positiveText(R.string.action_accept)
                .negativeText(R.string.action_cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        disableViews();
                        mTag.setName(mEditNameView.getText().toString().trim());
                        SaveTagJob job = Injection.sJobsComponent.saveTagJob();
                        job.setTag(mTag);
                        job.addTag(mSaveJobTag);
                        submitJob(job);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();

                        if (mContainerCallbacks != null) {
                            mContainerCallbacks.finish();
                        }
                    }
                })
                .autoDismiss(false)
                .customView(root, false)
                .build();

        mPositiveButton = mDialog.getActionButton(DialogAction.POSITIVE);
        mNegativeButton = mDialog.getActionButton(DialogAction.NEGATIVE);

        mEditNameView = (EditText) root.findViewById(android.R.id.edit);
        mEditNameView.setImeActionLabel(getString(R.string.ime_action_done),
                EditorInfo.IME_ACTION_DONE);
        mEditNameView.setOnEditorActionListener(mOnEditorActionListener);
        mEditNameView.addTextChangedListener(mTextWatcher);

        if (mEnteredText == null) {
            mEditNameView.setText(mTag.getName());
        }

        if (getJobEventDispatcher().isPending(mSaveJobTag)) {
            disableViews();
        }

        return mDialog;
    }
}
