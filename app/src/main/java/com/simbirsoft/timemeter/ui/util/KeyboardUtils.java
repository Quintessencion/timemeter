package com.simbirsoft.timemeter.ui.util;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;

public final class KeyboardUtils {

    public static void showSoftInput(Context context) {
        InputMethodManager imm = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void hideSoftInput(Context context, IBinder windowToken) {
        InputMethodManager imm = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(windowToken, 0);
    }

    public static void hideSoftInput(Activity activity) {
        hideSoftInput(activity, activity.getWindow().getDecorView().getWindowToken());
    }
}
