package com.simbirsoft.timemeter.ui.permission;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.base.BaseDialogFragment;
import com.simbirsoft.timemeter.util.PermissionUtils;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

@EFragment
public class WarningDenyPermissionDialog extends BaseDialogFragment{

    public interface OnSelectedActionListener {
        void onCancelRepeatAccessPermission();
        void onRepeatAccessPermission(PermissionUtils.PERMISSION type);
    }

    @FragmentArg
    public String message;

    private OnSelectedActionListener onSelectedAction;
    private PermissionUtils.PERMISSION type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setShouldSubscribeForJobEvents(false);
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.permission_dialog_title));
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.permission_dialog_ok_button), (dialogInterface, i) -> onClickPositiveButton());
        builder.setNegativeButton(getString(R.string.permission_dialog_cancel_button), (dialogInterface, i) -> onClickNegativeButton());
        return builder.create();
    }

    public void setOnSelectedAction(OnSelectedActionListener listener) {
        this.onSelectedAction = listener;
    }

    public void setType(PermissionUtils.PERMISSION type) {
        this.type = type;
    }

    private void onClickPositiveButton() {
        if (onSelectedAction != null) {
            onSelectedAction.onRepeatAccessPermission(type);
        }
    }

    private void onClickNegativeButton() {
        if (onSelectedAction != null) {
            onSelectedAction.onCancelRepeatAccessPermission();
        }
    }
}
