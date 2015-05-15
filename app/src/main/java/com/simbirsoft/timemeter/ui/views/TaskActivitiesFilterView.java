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

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.model.Period;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

@EViewGroup(R.layout.view_task_activities_filter)
public class TaskActivitiesFilterView extends FrameLayout implements
        DatePeriodView.DatePeriodViewListener {

    public interface OnSelectDateClickListener {
        void onSelectDateClicked(Calendar selectedDate);
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

        public SavedState(Parcel source) {
            super(source);
            mFilterState = source.readParcelable(SavedState.class.getClassLoader());
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeParcelable(mFilterState, 0);
        }
    }

    public static class FilterState implements Parcelable {

        public static final long PERIOD_MILLIS_DEFAULT = TimeUnit.DAYS.toMillis(1);

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
    private OnSelectDateClickListener mOnSelectDateClickListener;
    private boolean mIsSilentUpdate;

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

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.mFilterState = getFilterState();
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

        FilterState filterState = ss.mFilterState;

        if (filterState.startDateMillis != 0) {
           displayDatePeriod(filterState.startDateMillis, filterState.endDateMillis, filterState.period);
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
        }

        mDatePeriodView.setStartDateMillis(startDateMillis);
        //mDatePeriodView.setEndDateMillis(endDateMillis);
        mDatePeriodView.setPeriod(Period.MONTH);
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

    private FilterState getFilterState() {
        FilterState state = new FilterState();
        if (mDatePeriodView != null) {
            state.startDateMillis = mDatePeriodView.getStartDateMillis();
            state.endDateMillis = mDatePeriodView.getEndDateMillis();
            state.period = mDatePeriodView.getPeriod();
        } else {
            state.startDateMillis = 0;
            state.endDateMillis =0;
            state.period = null;
        }
        return state;
    }

    private void sendSelectDateClickEvent() {
        if (mOnSelectDateClickListener != null) {
            Calendar cal = Calendar.getInstance();
            long millis = (mDatePeriodView != null) ? mDatePeriodView.getSelectedDateMillis() : 0;
            cal.setTimeInMillis(millis == 0 ? System.currentTimeMillis() : millis);
            mOnSelectDateClickListener.onSelectDateClicked(cal);
        }
    }
}
