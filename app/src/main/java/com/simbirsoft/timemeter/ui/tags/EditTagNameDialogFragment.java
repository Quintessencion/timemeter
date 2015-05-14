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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.be.android.library.worker.util.JobSelector;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTagNamesJob;
import com.simbirsoft.timemeter.jobs.SaveTagJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseDialogFragment;
import com.simbirsoft.timemeter.ui.base.FragmentContainerCallbacks;

import org.slf4j.Logger;

import java.util.List;

import javax.inject.Inject;

public class EditTagNameDialogFragment extends BaseDialogFragment implements JobLoader.JobLoaderCallbacks {

    public static final String EXTRA_TAG = "extra_tag";
    public static final String LOAD_TAG_NAMES_JOB = "load_tag_names_job";

    private static final String STATE_ENTERED_TEXT = "entered_text";
    private static final Logger LOG = LogFactory.getLogger(EditTagNameDialogFragment.class);

    @Inject
    LoadTagNamesJob mLoadTagNamesJob;

    private EditText mEditNameView;
    private Tag mTag;
    private View mPositiveButton;
    private View mNegativeButton;
    private String mEnteredText;
    private FragmentContainerCallbacks mContainerCallbacks;
    private MaterialDialog mDialog;
    private final String mSaveJobTag = "save_tag_name";
    private List<String> mTagNames;

    private final TextView.OnEditorActionListener mOnEditorActionListener =
            (textView, actionId, keyEvent) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mPositiveButton.performClick();
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
        }

        @Override
        public void afterTextChanged(Editable editable) {
            checkInput();
        }
    };

    private void checkInput() {
        if (TextUtils.isEmpty(mEnteredText)) {
            setError(getString(R.string.error_tag_name_is_empty));
        }
        else if (mTagNames != null
                && !mTag.getName().equalsIgnoreCase(mEnteredText)
                && Iterables.indexOf(mTagNames, (tag) -> tag.equalsIgnoreCase(mEnteredText)) != -1) {
            setError(getString(R.string.error_tag_already_exists));
        }
        else {
            mPositiveButton.setEnabled(true);
        }
    }

    private void setError(String errorText) {
        mEditNameView.setError(errorText);
        mPositiveButton.setEnabled(false);
    }

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

        Injection.sUiComponent.injectEditTagNameDialogFragment(this);

        mTag = getArguments().getParcelable(EXTRA_TAG);
        Preconditions.checkArgument(mTag != null);

        if (savedInstanceState != null) {
            mEnteredText = savedInstanceState.getString(STATE_ENTERED_TEXT);
        }

        requestLoad(LOAD_TAG_NAMES_JOB, this);
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
    public void onTagSaveFailed(JobEvent event) {
        enableViews();
        if (event.getEventCode() == SaveTagJob.EVENT_CODE_TAG_ALREADY_EXISTS) {
            setError(getString(R.string.error_tag_already_exists));
        }
    }

    private void disableViews() {
        mPositiveButton.setEnabled(false);
        mNegativeButton.setEnabled(false);
        mEditNameView.setEnabled(false);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
    }

    @OnJobSuccess(LoadTagNamesJob.class)
    public void onTagNamesLoad(LoadJobResult<List<String>> result) {
        mTagNames = result.getData();
        checkInput();
    }

    @OnJobFailure(LoadTagNamesJob.class)
    public void onTagNamesLoadFailed() {
        LOG.error("Unable to load tag names");
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

        if (getJobEventDispatcher().isPending(JobSelector.forJobTags(mSaveJobTag))) {
            disableViews();
        }

        return mDialog;
    }

    @Override
    public Job onCreateJob(String tag) {
        switch (tag) {
            case LOAD_TAG_NAMES_JOB:
                return mLoadTagNamesJob;
            default:
                break;
        }
        throw new IllegalArgumentException("undefined tag");
    }
}
