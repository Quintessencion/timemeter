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
import android.widget.PopupWindow;
import android.widget.TextView;

import com.simbirsoft.timemeter.R;

public class CalendarPopupHelper {

    protected WindowManager mWindowManager;

    protected Context mContext;
    protected PopupWindow mWindow;

    protected View mView;

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
    }

    public CalendarPopupHelper(Context context) {
        this(context, R.layout.view_calendar_popup);

    }

    public void show(View anchor, Point anchorPoint) {
        preShow();
        int[] location = new int[2];

        anchor.getLocationOnScreen(location);
        anchorPoint.offset(location[0], location[1]);

        mView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        int rootHeight = mView.getMeasuredHeight();
        int rootWidth = mView.getMeasuredWidth();

        final int parentWidth = anchor.getWidth();
        final int parentHeight = anchor.getHeight();

        int yPos = anchorPoint.y - rootHeight;

        boolean onTop = true;

        if (anchorPoint.y < parentHeight / 2) {
            yPos = anchorPoint.y;
            onTop = false;
        }

        int xPos = 0;

        // ETXTREME RIGHT CLIKED
        if (anchorPoint.x + rootWidth > parentWidth) {
            xPos = (parentWidth - rootWidth);
        }
        // ETXTREME LEFT CLIKED
        else if (anchorPoint.x - (rootWidth / 2) < 0) {
            xPos = anchorPoint.x;
        }
        // INBETWEEN
        else {
            xPos = (anchorPoint.x - (rootWidth / 2));
        }

        mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);

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

        mWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
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
