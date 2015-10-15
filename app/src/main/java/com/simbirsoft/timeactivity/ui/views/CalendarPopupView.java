package com.simbirsoft.timeactivity.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.simbirsoft.timeactivity.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.view_calendar_popup)
public class CalendarPopupView extends RelativeLayout{

    @ViewById(android.R.id.list)
    RecyclerView mRecyclerView;

    @ViewById(R.id.upArrow)
    ImageView mUpArrowImage;

    @ViewById(R.id.downArrow)
    ImageView mDownArrowImage;

    @ViewById(R.id.leftArrow)
    ImageView mLeftArrowImage;

    @ViewById(R.id.rightArrow)
    ImageView mRightArrowImage;

    public CalendarPopupView(Context context) {
        super(context);
    }

    public CalendarPopupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CalendarPopupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CalendarPopupView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public ImageView getUpArrowImage() {
        return mUpArrowImage;
    }

    public ImageView getDownArrowImage() {
        return mDownArrowImage;
    }

    public ImageView getLeftArrowImage() {
        return mLeftArrowImage;
    }

    public ImageView getRightArrowImage() {
        return mRightArrowImage;
    }

    public void hideAllArrows() {
        mUpArrowImage.setVisibility(GONE);
        mDownArrowImage.setVisibility(GONE);
        mLeftArrowImage.setVisibility(GONE);
        mRightArrowImage.setVisibility(GONE);
    }
}
