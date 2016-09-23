package com.simbirsoft.timeactivity.ui.util;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public final class ToastUtils {

    private static final int DEFAULT_Y_TOAST_OFFSET_DIP = 36;

    public static void showToastWithAnchor(Context context, String toastText, View anchor, int toastLength) {
        int xOffset = 0;
        int yOffset = 0;
        Rect gvr = new Rect();

        if (anchor.getGlobalVisibleRect(gvr)) {
            View root = anchor.getRootView();

            int halfwayWidth = root.getRight() / 2;
            int halfwayHeight = root.getBottom() / 2;
            int parentX = (gvr.right - gvr.left) / 2 + gvr.left;
            int parentY = gvr.bottom; //(gvr.bottom - gvr.top) / 2 + gvr.top;

            if (parentY <= halfwayHeight) {
                yOffset = -(halfwayHeight - parentY);
            } else {
                int offset = (int) (context.getResources().getDisplayMetrics().scaledDensity
                        * DEFAULT_Y_TOAST_OFFSET_DIP);
                yOffset = gvr.top - halfwayHeight - offset;
            }
            if (parentX < halfwayWidth) {
                xOffset = -(halfwayWidth - parentX);
            }
            if (parentX >= halfwayWidth) {
                xOffset = parentX - halfwayWidth;
            }
        }

        Toast toast = Toast.makeText(context, toastText, toastLength);
        toast.setGravity(Gravity.CENTER, xOffset, yOffset);
        toast.show();
    }
}
