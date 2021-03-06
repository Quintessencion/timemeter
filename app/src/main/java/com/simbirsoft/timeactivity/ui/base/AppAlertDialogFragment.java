package com.simbirsoft.timeactivity.ui.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;

public class AppAlertDialogFragment extends BaseDialogFragment {

    public static final String EXTRA_DIALOG_TITLE = "title";
    public static final String EXTRA_DIALOG_MESSAGE = "message";
    public static final String EXTRA_DIALOG_ACCEPT_BUTTON_CAPTION = "accept_button_title";
    public static final String EXTRA_DIALOG_DECLINE_BUTTON_CAPTION = "decline_button_title";

    public static final int RESULT_CODE_ACCEPTED = 100;
    public static final int RESULT_CODE_CANCELLED = 110;

    public static Bundle prepareArgs(Context context, int dialogTitleRes, int dialogMessageRes,
                                     int acceptButtonCaptionRes, int declineButtonCaptionRes) {

        String dialogMessage = context.getString(dialogMessageRes);
        String dialogTitle = context.getString(dialogTitleRes);
        String acceptCaption = context.getString(acceptButtonCaptionRes);
        String declineCaption = context.getString(declineButtonCaptionRes);

        return prepareArgs(dialogTitle, dialogMessage, acceptCaption, declineCaption);
    }

    public static Bundle prepareArgs(Context context, int dialogTitleRes,
                                     int dialogMessageRes, int acceptButtonCaptionRes) {

        String dialogMessage = context.getString(dialogMessageRes);

        return prepareArgs(context, dialogTitleRes, dialogMessage, acceptButtonCaptionRes);
    }

    public static Bundle prepareArgs(Context context, int dialogTitleRes,
                                     String dialogMessage, int acceptButtonCaptionRes) {

        String dialogTitle = context.getString(dialogTitleRes);
        String acceptCaption = context.getString(acceptButtonCaptionRes);

        return prepareArgs(dialogTitle, dialogMessage, acceptCaption, null);
    }

    public static Bundle prepareArgs(String dialogTitle,
                                     String dialogMessage,
                                     String acceptButtonCaption) {

        return prepareArgs(dialogTitle, dialogMessage, acceptButtonCaption, null);
    }

    public static Bundle prepareArgs(String dialogTitle,
                                     String dialogMessage,
                                     String acceptButtonCaption,
                                     String declineButtonCaption) {

        Bundle args = new Bundle();
        args.putString(EXTRA_DIALOG_TITLE, dialogTitle);
        args.putString(EXTRA_DIALOG_MESSAGE, dialogMessage);
        args.putString(EXTRA_DIALOG_ACCEPT_BUTTON_CAPTION, acceptButtonCaption);
        args.putString(EXTRA_DIALOG_DECLINE_BUTTON_CAPTION, declineButtonCaption);

        return args;
    }

    private final MaterialDialog.ButtonCallback mDialogClickCallback =
            new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    sendResultAndFinish(RESULT_CODE_ACCEPTED);
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                    sendResultAndFinish(RESULT_CODE_CANCELLED);
                }
            };

    public String getTitle() {
        return getArguments().getString(EXTRA_DIALOG_TITLE);
    }

    public String getMessage() {
        return getArguments().getString(EXTRA_DIALOG_MESSAGE);
    }

    public String getAcceptButtonCaption() {
        return getArguments().getString(EXTRA_DIALOG_ACCEPT_BUTTON_CAPTION);
    }

    public String getCancelButtonCaption() {
        return getArguments().getString(EXTRA_DIALOG_DECLINE_BUTTON_CAPTION);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setShouldSubscribeForJobEvents(false);

        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());

        builder.title(getTitle());
        builder.content(getMessage());

        String acceptCaption = getAcceptButtonCaption();
        String cancelCaption = getCancelButtonCaption();

        if (!TextUtils.isEmpty(acceptCaption)) {
            builder.positiveText(acceptCaption);
        }

        if (!TextUtils.isEmpty(cancelCaption)) {
            builder.negativeText(cancelCaption);

        } else if (TextUtils.isEmpty(acceptCaption)) {
            builder.negativeText(android.R.string.cancel);
        }

        builder.cancelable(true);
        builder.callback(mDialogClickCallback);

        return builder.build();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        sendResultAndFinish(RESULT_CODE_CANCELLED);
    }

    private void sendResultAndFinish(int resultCode) {
        setResult(resultCode, null);
        finish();
    }
}
