package com.simbirsoft.timeactivity.ui.settings;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.simbirsoft.timeactivity.R;

public class TimePicker extends DialogPreference {
    public TimePicker (Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.view_time_picker_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }
}