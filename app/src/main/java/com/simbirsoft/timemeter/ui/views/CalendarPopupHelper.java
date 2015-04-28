package com.simbirsoft.timemeter.ui.views;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.simbirsoft.timemeter.R;

public class CalendarPopupHelper {

    protected WindowManager mWindowManager;

    protected Context mContext;
    protected PopupWindow mWindow;

    protected View mView;
    protected LinearLayout mLinearLayout;

    protected Drawable mBackgroundDrawable = null;
    protected ShowListener showListener;

    public CalendarPopupHelper(Context context, int viewResource) {
        mContext = context;
        mWindow = new PopupWindow(context);

        mWindowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);


        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setContentView(layoutInflater.inflate(viewResource, null));
        mLinearLayout = (LinearLayout)mView.findViewById(R.id.popupLinearLayout);
    }

    public CalendarPopupHelper(Context context) {
        this(context, R.layout.view_calendar_popup);
    }

    public void show(View anchor, Point anchorPoint) {
        preShow();
        int[] location = new int[2];

        for (int i = 0; i < 20; i++) {
            TextView tv= new TextView(mContext);
            tv.setText(String.format("Item %d", i));
            mLinearLayout.addView(tv);
        }

        anchor.getLocationOnScreen(location);

        mLinearLayout.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        int rootHeight = mLinearLayout.getMeasuredHeight();
        int rootWidth = mLinearLayout.getMeasuredWidth();

        final int anchorWidth = anchor.getWidth();
        final int anchorHeight = anchor.getHeight();

        rootWidth = Math.min(rootWidth, anchorWidth);
        rootHeight = Math.min(rootHeight, anchorHeight);

        int yPos = 0;
        boolean onTop = true;

        if (anchorPoint.y < anchorHeight / 2) {
            yPos = anchorPoint.y;
            rootHeight = Math.min(rootHeight, anchorHeight - yPos);
            onTop = false;
        } else {
            rootHeight = Math.min(anchorPoint.y, rootHeight);
            yPos = anchorPoint.y - rootHeight;
        }

        int xPos = 0;

        // ETXTREME RIGHT CLIKED
        if (anchorPoint.x + rootWidth > anchorWidth) {
            xPos = (anchorWidth - rootWidth);
        }
        // ETXTREME LEFT CLIKED
        else if (anchorPoint.x - (rootWidth / 2) < 0) {
            xPos = anchorPoint.x;
        }
        // INBETWEEN
        else {
            xPos = (anchorPoint.x - (rootWidth / 2));
        }
        mWindow.setWidth(Math.min(rootWidth, anchorWidth - xPos));
        mWindow.setHeight(rootHeight);
        mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos + location[0], yPos + location[1]);
    }

    protected void preShow() {
        if (mView == null)
            throw new IllegalStateException("view undefined");



        if (showListener != null) {
            showListener.onPreShow();
            showListener.onShow();
        }

        if (mBackgroundDrawable == null)
            mWindow.setBackgroundDrawable(new BitmapDrawable());
        else
            mWindow.setBackgroundDrawable(mBackgroundDrawable);

        mWindow.setTouchable(true);
        mWindow.setFocusable(true);
        mWindow.setOutsideTouchable(true);

        mWindow.setContentView(mView);
    }

    public void setBackgroundDrawable(Drawable background) {
        mBackgroundDrawable = background;
    }

    public void setContentView(View root) {
        mView = root;

        mWindow.setContentView(root);
    }

    public void setContentView(int layoutResID) {
        LayoutInflater inflator = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setContentView(inflator.inflate(layoutResID, null));
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        mWindow.setOnDismissListener(listener);
    }

    public void dismiss() {
        mWindow.dismiss();
        if (showListener != null) {
            showListener.onDismiss();
        }
    }

    public static interface ShowListener {
        void onPreShow();
        void onDismiss();
        void onShow();
    }

    public void setShowListener(ShowListener showListener) {
        this.showListener = showListener;
    }
}
