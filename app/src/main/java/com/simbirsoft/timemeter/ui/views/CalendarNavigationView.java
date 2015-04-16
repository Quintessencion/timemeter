package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.log.LogFactory;
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

    @ViewById(R.id.firstTextView)
    TextView mFirstTextView;

    @ViewById(R.id.secondTextView)
    TextView mSecondTextView;

    @ViewById(R.id.prevButton)
    ImageButton mPrevButton;

    @ViewById(R.id.nextButton)
    ImageButton mNextButton;

    @ViewById(R.id.calendarTextLayout)
    LinearLayout mTextLayout;

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
        mCalendarPeriod = new CalendarPeriod();
        final Resources res = getContext().getResources();
        final DisplayMetrics displayMetrics = res.getDisplayMetrics();
        mTextPadding = (int) (displayMetrics.density * TEXT_PADDING_DEFAULT_DIP);
        update();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Paint firstPaint = mFirstTextView.getPaint();
        Paint secondPaint = mSecondTextView.getPaint();
        float width = 0;
        List<String> firstTextString = mCalendarPeriod.getFirstTestString();
        List<String> secondTextString = mCalendarPeriod.getSecondTestString();
        for (String test : firstTextString) {
            width = Math.max(width, firstPaint.measureText(test));
        }
        for (String test : secondTextString) {
            width = Math.max(width, secondPaint.measureText(test));
        }
        mTextLayout.setMinimumWidth((int)width  + 2 * mTextPadding);
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
        mFirstTextView.setText(mCalendarPeriod.getPeriodFirstString());
        mSecondTextView.setText(mCalendarPeriod.getPeriodSecondString());
        mNextButton.setEnabled(mCalendarPeriod.canMoveNext());
        mPrevButton.setEnabled(mCalendarPeriod.canMovePrev());
    }
}
