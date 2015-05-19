package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.model.Period;
import com.simbirsoft.timemeter.ui.util.ToastUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

@EViewGroup(R.layout.view_task_activities_filter)
public class TaskActivitiesFilterView extends FrameLayout implements
        DatePeriodView.DatePeriodViewListener {

    public interface OnTaskActivitiesFilterListener {
        void onSelectDateClicked(Calendar selectedDate);
        void onFilterChanged(FilterState filterState);
        void onFilterReset();
    }

    private static class SavedState extends BaseSavedState {

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        FilterState mFilterState;
        int mSelectedDatePanel;

        public SavedState(Parcel source) {
            super(source);
            mFilterState = source.readParcelable(SavedState.class.getClassLoader());
            mSelectedDatePanel = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeParcelable(mFilterState, 0);
            dest.writeInt(mSelectedDatePanel);
        }
    }

    public static class FilterState implements Parcelable {

        public static final Creator<FilterState> CREATOR =
                new Creator<FilterState>() {
                    @Override
                    public FilterState createFromParcel(Parcel parcel) {
                        return new FilterState(parcel);
                    }

                    @Override
                    public FilterState[] newArray(int sz) {
                        return new FilterState[sz];
                    }
                };

        public long startDateMillis;
        public long endDateMillis;
        public Period period;

        private FilterState() {
        }

        public FilterState copy() {
            FilterState state = new FilterState();
            state.startDateMillis = startDateMillis;
            state.endDateMillis = endDateMillis;
            state.period = period;
            return state;
        }

        public boolean isEmpty() {
            return startDateMillis == 0
                    && endDateMillis == 0
                    && period == null;
        }

        private FilterState(Parcel source) {
            startDateMillis = source.readLong();
            endDateMillis = source.readLong();
            if (source.readByte() == 1) {
                period = Period.valueOf(source.readString());
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeLong(startDateMillis);
            parcel.writeLong(endDateMillis);
            parcel.writeByte((byte) (period == null ? 0 : 1));
            if (period != null) {
                parcel.writeString(period.name());
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FilterState that = (FilterState) o;

            if (startDateMillis != that.startDateMillis) return false;
            if (endDateMillis != that.endDateMillis) return false;
            if (period != that.period) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (startDateMillis ^ (startDateMillis >>> 32));
            result = 31 * result + (int) (endDateMillis ^ (endDateMillis >>> 32));
            result = 31 * result + (period != null ? period.hashCode() : 0);
            return result;
        }
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
    private OnTaskActivitiesFilterListener mOnFilterListener;
    private FilterState mFilterState;

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
        mFilterState = new FilterState();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.mFilterState = mFilterState;
        ss.mSelectedDatePanel = mDatePeriodView != null
                ? mDatePeriodView.getSelectedDatePanel()
                : DatePeriodView.DATE_PANEL_NONE;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mFilterState = ss.mFilterState;

        if (mFilterState.startDateMillis != 0) {
            displayDatePeriod(mFilterState.startDateMillis, mFilterState.endDateMillis, mFilterState.period);
            mDatePeriodView.setSelectedDatePanel(ss.mSelectedDatePanel);
            postFilterUpdate();
        }
    }

    @Click(R.id.chooseDateView)
    void onChooseDateClicked() {
        sendSelectDateClickEvent();
    }

    @Click(R.id.resetFilterView)
    void onResetFilterClicked() {
        hideDatePeriod();
        updateFilterState();
        if (mOnFilterListener != null) {
            mOnFilterListener.onFilterReset();
        }
    }

    @LongClick(R.id.resetFilterView)
    void onResetFilterLongClicked(View v) {
        ToastUtils.showToastWithAnchor(getContext(),
                mHintResetFilter, v, Toast.LENGTH_SHORT);
    }

    @Override
    public void onDateTextClicked() {
        sendSelectDateClickEvent();
    }

    @Override
    public void onPeriodSelected(Period period) {
        postFilterUpdate();
    }

    @Override
    public void onDateReset() {
        hideDatePeriod();
        postFilterUpdate();
    }

    public void setOnFilterListener(OnTaskActivitiesFilterListener onFilterListener) {
        mOnFilterListener = onFilterListener;
    }

    public FilterState getFilterState() {
        return mFilterState;
    }

    public void setDate(long dateMillis) {
        if (mDatePeriodView == null) {
            displayDatePeriod(dateMillis, 0, Period.ALL);
        } else {
            mDatePeriodView.setSelectedDateMillis(dateMillis);
        }
        postFilterUpdate();
    }

    private void displayDatePeriod(long startDateMillis, long endDateMillis, Period period) {
        if (mDatePeriodView == null) {
            mDatePeriodView = (DatePeriodView) LayoutInflater.from(getContext())
                    .inflate(R.layout.view_activities_date_period_composed, mDatePanel, false);

            mDatePanel.removeView(mChooseDateView);
            mDatePanel.addView(mDatePeriodView);
            mDatePeriodView.setDatePeriodViewListener(this);
        }
        mDatePeriodView.setValues(startDateMillis, endDateMillis, period);
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
        updateFilterState();
        if (mOnFilterListener != null) {
            mOnFilterListener.onFilterChanged(mFilterState);
        }
    }

    private void updateFilterState() {
        if (mDatePeriodView != null) {
            mFilterState.startDateMillis = mDatePeriodView.getStartDateMillis();
            mFilterState.endDateMillis = mDatePeriodView.getEndDateMillis();
            mFilterState.period = mDatePeriodView.getPeriod();
        } else {
            mFilterState.startDateMillis = 0;
            mFilterState.endDateMillis =0;
            mFilterState.period = null;
        }
    }

    private void sendSelectDateClickEvent() {
        if (mOnFilterListener != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(mDatePeriodView != null ? mDatePeriodView.getInitialValueForSelectedDate() : System.currentTimeMillis());
            mOnFilterListener.onSelectDateClicked(cal);
        }
    }
}
