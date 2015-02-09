package com.simbirsoft.timemeter.ui.util;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.simbirsoft.timemeter.R;

public final class TagViewUtils {

    public static TextView inflateTagView(LayoutInflater inflater, ViewGroup root, int tagColor) {
        TextView view =  (TextView) inflater.inflate(R.layout.view_tag_small, root, false);

        updateTagViewColor(view, tagColor);

        return view;
    }

    public static void updateTagViewColor(View taggedView, int tagColor) {
        GradientDrawable bg = (GradientDrawable) taggedView.getBackground();
        bg.setColor(tagColor);
    }
}
