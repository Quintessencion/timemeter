package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.model.CalendarPeriod;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.Date;
import java.util.List;

@EViewGroup(R.layout.view_calendar_navigation)
public class CalendarNavigationView extends RelativeLayout{
    public interface OnCalendarNavigateListener {
        public void onMovedNext(Date newStartDate, Date newEndDate);
        public void onMovedPrev(Date newStartDate, Date newEndDate);
    }


    private static final int TEXT_PADDING_DEFAULT_DIP = 2;

    @ViewById(R.id.periodTextView)
    TextView mTextView;

    @ViewById(R.id.prevButton)
    ImageView mPrevButton;

    @ViewById(R.id.nextButton)
    ImageView mNextButton;

    @ViewById(R.id.nextButtonRipple)
    MaterialRippleLayout mNextButtonBackground;

    @ViewById(R.id.prevButtonRipple)
    MaterialRippleLayout mPrevButtonBackground;

    private int mTextPadding;

    private CalendarPeriod mCalendarPeriod;

    private OnCalendarNavigateListener mOnCalendarNavigateListener;

    public CalendarNavigationView(Context context) {
        super(context);
    }

    public CalendarNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CalendarNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CalendarNavigationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @AfterViews
    void bindViews() {
        final Resources res = getContext().getResources();
        final DisplayMetrics displayMetrics = res.getDisplayMetrics();
        mTextPadding = (int) (displayMetrics.density * TEXT_PADDING_DEFAULT_DIP);
        mCalendarPeriod = new CalendarPeriod();
        update();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Paint paint = mTextView.getPaint();
        float width = 0;
        List<String> strings = mCalendarPeriod.getTestStrings();
        for (String test : strings) {
            width = Math.max(width, paint.measureText(test));
        }
        mTextView.setMinimumWidth((int)width  + 2 * mTextPadding);
        mPrevButtonBackground.setRippleRoundedCorners(mPrevButtonBackground.getMeasuredWidth() / 2);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mPrevButtonBackground.setRippleRoundedCorners(mPrevButtonBackground.getWidth() / 2);
        mNextButtonBackground.setRippleRoundedCorners(mNextButtonBackground.getWidth() / 2);
    }

    @Click(R.id.nextButton)
    void nextButtonClicked() {
        mCalendarPeriod.moveNext();
        update();
        if (mOnCalendarNavigateListener != null) {
            mOnCalendarNavigateListener.onMovedNext(mCalendarPeriod.getStartDate(), mCalendarPeriod.getEndDate());
        }
    }

    @Click(R.id.prevButton)
    void prevButtonClicked() {
        mCalendarPeriod.movePrev();
        update();
        if (mOnCalendarNavigateListener != null) {
            mOnCalendarNavigateListener.onMovedPrev(mCalendarPeriod.getStartDate(), mCalendarPeriod.getEndDate());
        }
    }

    public CalendarPeriod getCalendarPeriod() {
        return mCalendarPeriod;
    }

    public void setCalendarPeriod(CalendarPeriod period) {
        mCalendarPeriod = period;
        update();
    }

    public OnCalendarNavigateListener getOnCalendarNavigateListener() {
        return mOnCalendarNavigateListener;
    }

    public void setOnCalendarNavigateListener(OnCalendarNavigateListener onCalendarNavigateListener) {
        mOnCalendarNavigateListener = onCalendarNavigateListener;
    }


    private void update() {
        mTextView.setText(mCalendarPeriod.getPeriodString());
        mNextButtonBackground.setVisibility(mCalendarPeriod.canMoveNext() ? VISIBLE : INVISIBLE);
        mPrevButtonBackground.setVisibility(mCalendarPeriod.canMovePrev() ? VISIBLE : INVISIBLE);
    }
}
