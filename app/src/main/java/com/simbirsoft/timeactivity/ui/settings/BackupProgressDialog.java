package com.simbirsoft.timeactivity.ui.settings;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.ui.base.BaseDialogFragment;

public abstract class BackupProgressDialog extends BaseDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ProgressDialog backupDialog = new ProgressDialog(getActivity());
        backupDialog.setTitle(getResources().getString(getTitleResId()));
        backupDialog.setMessage(getResources().getString(R.string.backup_dialog_message));
        backupDialog.setIndeterminate(true);
        backupDialog.setCanceledOnTouchOutside(false);
        return backupDialog;
    }

    protected void displayMessage(int resId) {
        showToast(resId, false);
        this.dismiss();
    }

    protected void displayMessage(String message) {
        showToast(message, false);
        this.dismiss();
    }

    public abstract int getTitleResId();
}