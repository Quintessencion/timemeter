package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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


    private static final int TEXT_PADDING_DEFAULT_DIP = 5;

    @ViewById(R.id.periodTextView)
    TextView mTextView;

    @ViewById(R.id.prevButton)
    ImageButton mPrevButton;

    @ViewById(R.id.nextButton)
    ImageButton mNextButton;

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
        mNextButton.setVisibility(mCalendarPeriod.canMoveNext() ? VISIBLE : INVISIBLE);
        mPrevButton.setVisibility(mCalendarPeriod.canMovePrev() ? VISIBLE : INVISIBLE);
    }
}
