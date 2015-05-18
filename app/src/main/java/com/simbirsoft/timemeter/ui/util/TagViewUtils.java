package com.simbirsoft.timemeter.ui.util;

import android.graphics.drawable.GradientDrawable;
import android.view.View;

public final class TagViewUtils {

    public static void updateTagViewColor(View taggedView, int tagColor) {
        GradientDrawable bg = (GradientDrawable) taggedView.getBackground();
        bg.setColor(tagColor);
    }
}
