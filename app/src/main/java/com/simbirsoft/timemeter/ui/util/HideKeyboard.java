package com.simbirsoft.timemeter.ui.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class HideKeyboard {

    private Activity context;
    private InputMethodManager inputMethodManager;

    public HideKeyboard(Activity context) {
        this.context = context;
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void hide(View view) {
        if (view == null) {
            return;
        }

        if(!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                if (context.getCurrentFocus() != null) {
                    inputMethodManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
                }
                return false;
            });
        }

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                hide(innerView);
            }
        }
    }
}
