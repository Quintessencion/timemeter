package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.events.FilterViewStateChangeEvent;
import com.simbirsoft.timemeter.model.Period;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.Calendar;

@EViewGroup(R.layout.view_task_activities_filter)
public class TaskActivitiesFilterView extends FrameLayout implements
        DatePeriodView.DatePeriodViewListener {

    public interface OnSelectDateClickListener {
        void onSelectDateClicked(Calendar selectedDate);
    }

    @ViewById(R.id.shadowUp)
    View mShadowUp;

    @ViewById(R.id.shadowDown)
    View mShadowDown;

    @ViewById(R.id.chooseDateView)
    View mChooseDateView;

    @ViewById(R.id.datePanel)
    ViewGroup mDatePanel;

    @StringRes(R.string.hint_reset_filter)
    String mHintResetFilter;

    private DatePeriodView mDatePeriodView;
    private OnSelectDateClickListener mOnSelectDateClickListener;

    public TaskActivitiesFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TaskActivitiesFilterView(Context context) {
        super(context);
    }

    public TaskActivitiesFilterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TaskActivitiesFilterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @AfterViews
    void initializeView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Hide custom elevation on Lollipop
            mShadowDown.setVisibility(View.GONE);
            mShadowUp.setVisibility(View.GONE);
        }
    }

    @Click(R.id.chooseDateView)
    void onChooseDateClicked() {
        sendSelectDateClickEvent();
    }

    @Override
    public void onDateTextClicked() {
        sendSelectDateClickEvent();
    }

    @Override
    public void onPeriodSelected(Period period) {
        //mState.period = period;
        postFilterUpdate();
    }

    @Override
    public void onDateReset() {
        hideDatePeriod();
    }

    public void setOnSelectDateClickListener(OnSelectDateClickListener onSelectDateClickListener) {
        mOnSelectDateClickListener = onSelectDateClickListener;
    }

    public void setDate(long dateMillis) {
        //mState.dateMillis = dateMillis;

        boolean needPostUpdate = mDatePeriodView != null;
        displayDatePeriod();
        if (needPostUpdate) {
            postFilterUpdate();
        }
    }

    private void displayDatePeriod() {
        if (mDatePeriodView == null) {
            mDatePeriodView = (DatePeriodView) LayoutInflater.from(getContext())
                    .inflate(R.layout.view_date_period_composed, mDatePanel, false);

            mDatePanel.removeView(mChooseDateView);
            mDatePanel.addView(mDatePeriodView);
        }

        //mDatePeriodView.setDateMillis(mState.dateMillis);
        mDatePeriodView.setDateMillis(System.currentTimeMillis());
        mDatePeriodView.setDatePeriodViewListener(this);
    }

    private void hideDatePeriod() {
        if (mDatePeriodView != null) {
            mDatePanel.removeView(mDatePeriodView);
            mDatePanel.addView(mChooseDateView);
        }
        mDatePeriodView = null;
        postFilterUpdate();
    }

    private void postFilterUpdate() {

    }

    private void sendSelectDateClickEvent() {
        if (mOnSelectDateClickListener != null) {
            Calendar cal = Calendar.getInstance();
            //cal.setTimeInMillis(mState.dateMillis == 0 ? System.currentTimeMillis() : mState.dateMillis);
            cal.setTimeInMillis(System.currentTimeMillis());
            mOnSelectDateClickListener.onSelectDateClicked(cal);
        }
    }
}
