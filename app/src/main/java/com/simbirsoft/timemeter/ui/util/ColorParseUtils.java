package com.simbirsoft.timemeter.ui.util;

import android.graphics.Color;

public class ColorParseUtils {

    public static final int DEFAULT_COLOR = -10453621; /* blue grey #607D8B */

    public static int parseColor(String color) {
        try {
            return Color.parseColor(color);
        }
        catch (IllegalArgumentException | NullPointerException e) {
            return DEFAULT_COLOR;
        }
    }

    public static String parseColor(int color) {
        return "#" + Integer.valueOf(String.valueOf(color), 16);
    }
}
