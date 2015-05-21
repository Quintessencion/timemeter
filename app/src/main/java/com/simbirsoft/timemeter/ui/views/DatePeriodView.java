package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.model.Period;
import com.simbirsoft.timemeter.ui.util.TagViewUtils;
import com.simbirsoft.timemeter.ui.util.TimeUtils;
import com.simbirsoft.timemeter.ui.util.ToastUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.slf4j.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

@EViewGroup(R.layout.view_date_period)
public class DatePeriodView extends FrameLayout {

    interface DatePeriodViewListener {
        void onDateTextClicked();
        void onPeriodSelected(Period period);
        void onDateReset();
    }

    @IntDef({DATE_PANEL_NONE, DATE_PANEL_START, DATE_PANEL_END})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DatePanelType {}

    public static final int DATE_PANEL_NONE = 0;
    public static final int DATE_PANEL_START = 1;
    public static final int DATE_PANEL_END = 2;

    private static final Logger LOG = LogFactory.getLogger(DatePeriodView.class);

    private static final String EXTRA_SUPER_STATE = "super_state";
    private static final String EXTRA_START_DATE_MILLIS = "start_date_millis";
    private static final String EXTRA_END_DATE_MILLIS = "end_date_millis";

    private static final List<Period> mPeriodsDefinition = Arrays.asList(
            Period.DAY,
            Period.WEEK,
            Period.MONTH,
            Period.YEAR,
            Period.ALL,
            Period.OTHER);

    private static final int DEFAULT_ITEM_SRC = R.array.period_array;

    @ViewById(R.id.periodStartPanel)
    DatePanelView mPeriodStartPanel;

    @ViewById(R.id.periodEndPanel)
    DatePanelView mPeriodEndPanel;


    @ViewById(R.id.periodSpinner)
    Spinner mPeriodSpinner;

    @StringRes(R.string.hint_choose_filter_period)
    String mHintChooseFilterPeriod;

    private ArrayAdapter<CharSequence> mAdapter;
    private DatePeriodViewListener mDatePeriodViewListener;
    private List<String> mPeriodList;
    private Period mSelectedPeriod;
    private int mItemsSrc;
    private final Calendar mCalendar = Calendar.getInstance();
    @DatePanelType
    private int mSelectedDatePanel;

    @LongClick(R.id.periodSpinner)
    void onPeriodSpinnerLongClicked(View v) {
        ToastUtils.showToastWithAnchor(getContext(),
                mHintChooseFilterPeriod, v, Toast.LENGTH_SHORT);
    }

    @ItemSelect(R.id.periodSpinner)
    void onPeriodSelected(boolean selected, CharSequence selectedPeriod) {
        if (selected) {
            LOG.info("selected period: {}", selectedPeriod);
			Period period = getPeriod();
            if (Objects.equal(mSelectedPeriod, period)) {
                return;
            }

            if (period == Period.OTHER) {
            	sendOnDateClicked(DATE_PANEL_END);
            	setPeriod(mSelectedPeriod);
        	} else {
            	mSelectedPeriod = period;
            	sendOnPeriodSelected();
        	}
        }
    }

    public DatePeriodView(Context context) {
        super(context);
        mItemsSrc = DEFAULT_ITEM_SRC;
    }

    public DatePeriodView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCustomAttributes(context, attrs);
    }

    public DatePeriodView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCustomAttributes(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DatePeriodView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initCustomAttributes(context, attrs);
    }

    public DatePeriodViewListener getDatePeriodViewListener() {
        return mDatePeriodViewListener;
    }

    public void setDatePeriodViewListener(DatePeriodViewListener datePeriodViewListener) {
        mDatePeriodViewListener = datePeriodViewListener;
    }

    @AfterViews
    void initializeView() {
        Resources res = getContext().getResources();
        mPeriodList = Arrays.asList(res.getStringArray(mItemsSrc));
        mAdapter = ArrayAdapter.createFromResource(getContext(),
                mItemsSrc, android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mPeriodSpinner.setAdapter(mAdapter);
        int tagColor = res.getColor(R.color.taggedColor);
        TagViewUtils.updateTagViewColor(mPeriodStartPanel, tagColor);
        TagViewUtils.updateTagViewColor(mPeriodEndPanel, tagColor);
        mSelectedPeriod = getPeriod();

        mPeriodStartPanel.setDatePanelViewListener(new DatePanelView.DatePanelViewListener() {
            @Override
            public void onDateTextClicked() {
                LOG.debug("start date text clicked");
                sendOnDateClicked(DATE_PANEL_START);
            }

            @Override
            public void onDateReset() {
                LOG.debug("reset start date period clicked");
                if (mDatePeriodViewListener != null) {
                    mDatePeriodViewListener.onDateReset();
                }
            }
        });

        mPeriodEndPanel.setVisibility(GONE);
        mPeriodEndPanel.setDatePanelViewListener(new DatePanelView.DatePanelViewListener() {
            @Override
            public void onDateTextClicked() {
                LOG.debug("end date text clicked");
                sendOnDateClicked(DATE_PANEL_END);
            }

            @Override
            public void onDateReset() {
                LOG.debug("reset end date period clicked");
                resetEndDate();
                sendOnPeriodSelected();
            }
        });
    }

    public Period getPeriod() {
        CharSequence item = (CharSequence) mPeriodSpinner.getSelectedItem();
        int index = Iterables.indexOf(mPeriodList, (s) -> Objects.equal(item, s));
        return mPeriodsDefinition.get(index);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState =  super.onSaveInstanceState();

        Bundle state = new Bundle();

        state.putParcelable(EXTRA_SUPER_STATE, superState);
        state.putLong(EXTRA_START_DATE_MILLIS, mPeriodStartPanel.getDateMillis());
        state.putLong(EXTRA_END_DATE_MILLIS, mPeriodEndPanel.getDateMillis());
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(((Bundle) state).getParcelable(EXTRA_SUPER_STATE));
        mPeriodStartPanel.setDateMillis(((Bundle) state).getLong(EXTRA_START_DATE_MILLIS));
        mPeriodEndPanel.setDateMillis(((Bundle) state).getLong(EXTRA_END_DATE_MILLIS));
    }

    public long getStartDateMillis() {
        return mPeriodStartPanel.getDateMillis();
    }

    public long getEndDateMillis() {
        return mPeriodEndPanel.getDateMillis();
    }

    public void setStartDateMillis(long dateMillis) {
        mPeriodStartPanel.setDateMillis(dateMillis);
    }

    public void setEndDateMillis(long dateMillis) {
        mPeriodEndPanel.setDateMillis(dateMillis);
        if (mSelectedPeriod != Period.OTHER) {
            setPeriod(Period.OTHER);
        }
    }

    public void setSelectedDateMillis(long dateMillis) {
        switch (mSelectedDatePanel) {
            case DATE_PANEL_START:
                setStartDateMillis(dateMillis);
                break;

            case DATE_PANEL_END:
                setEndDateMillis(dateMillis);
                break;
        }
        mSelectedDatePanel = DATE_PANEL_NONE;
    }

    public long getInitialValueForSelectedDate() {
        switch (mSelectedDatePanel) {
            case DATE_PANEL_START:
                return (mPeriodStartPanel.getDateMillis() != 0) ? mPeriodStartPanel.getDateMillis() : System.currentTimeMillis();

            case DATE_PANEL_END:
                return (mPeriodEndPanel.getDateMillis() != 0) ? mPeriodEndPanel.getDateMillis() : mPeriodStartPanel.getDateMillis();
        }
        return 0;
    }

    public boolean checkStartDateNewValue(long dateMillis) {
        return (mSelectedPeriod == Period.OTHER)
                ? datesIsNormal(dateMillis, mPeriodEndPanel.getDateMillis())
                : true;
    }

    public boolean checkEndDateNewValue(long dateMillis) {
        return datesIsNormal(mPeriodStartPanel.getDateMillis(), dateMillis);
    }

    public boolean checkSelectedDateNewValue(long dateMillis) {
        switch (mSelectedDatePanel) {
            case DATE_PANEL_START:
                return checkStartDateNewValue(dateMillis);

            case DATE_PANEL_END:
                return checkEndDateNewValue(dateMillis);
        }
        return false;
    }

    @DatePanelType
    public int getSelectedDatePanel() {
        return mSelectedDatePanel;
    }

    public void setSelectedDatePanel(@DatePanelType int panel) {
        mSelectedDatePanel = panel;
    }

    public void setPeriod(Period period) {
        int index = Iterables.indexOf(mPeriodsDefinition, input -> input == period);

        if (index < 0) {
            throw new IllegalArgumentException(String.format("period '%s' is not defined", period));
        }

        mSelectedPeriod = period;
        mPeriodSpinner.setSelection(index);
        if (period == Period.OTHER) {
            mPeriodSpinner.setVisibility(GONE);
            mPeriodEndPanel.setVisibility(VISIBLE);
        } else {
            mPeriodEndPanel.setVisibility(GONE);
            mPeriodSpinner.setVisibility(VISIBLE);
        }
    }

    public void setValues(long startDate, long endDate, Period period) {
        mPeriodStartPanel.setDateMillis(startDate);
        mPeriodEndPanel.setDateMillis(endDate);
        setPeriod(period);
    }

    private void resetEndDate() {
        mPeriodEndPanel.setDateMillis(0);
        setPeriod(Period.ALL);
    }

    private boolean datesIsNormal(long startDateMillis, long endDateMillis) {
        mCalendar.setTimeInMillis(startDateMillis);
        long dateMillis = TimeUtils.getDayStartMillis(mCalendar);
        mCalendar.setTimeInMillis(endDateMillis);
        return TimeUtils.getDayStartMillis(mCalendar) >= dateMillis;
    }

    private void sendOnDateClicked(@DatePanelType int panel) {
        mSelectedDatePanel = panel;
        if (mDatePeriodViewListener != null) {
            mDatePeriodViewListener.onDateTextClicked();
        }
    }

    private void sendOnPeriodSelected() {
        if (mDatePeriodViewListener != null) {
            mDatePeriodViewListener.onPeriodSelected(getPeriod());
        }
    }

    private void initCustomAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DatePeriodView,
                0, 0);

        try {
            mItemsSrc = a.getResourceId(R.styleable.DatePeriodView_itemsSrc, DEFAULT_ITEM_SRC);
        } finally {
            a.recycle();
        }
    }
}
