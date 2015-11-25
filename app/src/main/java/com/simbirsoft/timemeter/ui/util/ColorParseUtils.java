package com.simbirsoft.timemeter.ui.util;

import android.graphics.Color;

import com.simbirsoft.timemeter.db.model.Tag;

public class ColorParseUtils {

    public static int parseColor(String color) {
        try {
            return Color.parseColor(color);
        }
        catch (IllegalArgumentException | NullPointerException e) {
            return Tag.DEFAULT_COLOR;
        }
    }

    public static String parseColor(int color) {
        return "#" + Integer.valueOf(String.valueOf(color), 16);
    }
}
