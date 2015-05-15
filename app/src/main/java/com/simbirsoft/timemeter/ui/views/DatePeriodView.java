package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.simbirsoft.timemeter.ui.util.ToastUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

@EViewGroup(R.layout.view_date_period)
public class DatePeriodView extends FrameLayout {

    interface DatePeriodViewListener {
        void onDateTextClicked();
        void onPeriodSelected(Period period);
        void onDateReset();
    }

    private static final Logger LOG = LogFactory.getLogger(DatePeriodView.class);

    private static final String EXTRA_SUPER_STATE = "super_state";
    private static final String EXTRA_DATE_MILLIS = "date_millis";

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
    private int mItemsSrc;
    private DatePanelView mSelectedDatePanel;

    @LongClick(R.id.periodSpinner)
    void onPeriodSpinnerLongClicked(View v) {
        ToastUtils.showToastWithAnchor(getContext(),
                mHintChooseFilterPeriod, v, Toast.LENGTH_SHORT);
    }

    @ItemSelect(R.id.periodSpinner)
    void onPeriodSelected(boolean selected, CharSequence selectedPeriod) {
        if (selected) {
            LOG.info("selected period: {}", selectedPeriod);
            if (getPeriod() == Period.OTHER) {
                mPeriodSpinner.setVisibility(GONE);
                mPeriodEndPanel.setVisibility(VISIBLE);
                sendOnDateClicked(mPeriodEndPanel);
            } else {
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

        mPeriodStartPanel.setDatePanelViewListener(new DatePanelView.DatePanelViewListener() {
            @Override
            public void onDateTextClicked() {
                LOG.debug("start date text clicked");
                sendOnDateClicked(mPeriodStartPanel);
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
                sendOnDateClicked(mPeriodEndPanel);
            }

            @Override
            public void onDateReset() {
                LOG.debug("reset end date period clicked");
                mPeriodEndPanel.setDateMillis(0);
                mPeriodEndPanel.setVisibility(GONE);
                setPeriod(Period.ALL);
                mPeriodSpinner.setVisibility(VISIBLE);
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
        state.putLong(EXTRA_DATE_MILLIS, mPeriodStartPanel.getDateMillis());

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(((Bundle) state).getParcelable(EXTRA_SUPER_STATE));
        mPeriodStartPanel.setDateMillis(((Bundle) state).getLong(EXTRA_DATE_MILLIS));
    }

    public long getStartDateMillis() {
        return mPeriodStartPanel.getDateMillis();
    }

    public long getEndDateMillis() {
        return mPeriodStartPanel.getDateMillis();
    }

    public void setStartDateMillis(long dateMillis) {
        mPeriodStartPanel.setDateMillis(dateMillis);
    }

    public void setEndDateMillis(long dateMillis) {
        mPeriodEndPanel.setDateMillis(dateMillis);
    }

    public long getSelectedDateMillis() {
        return (mSelectedDatePanel != null) ? mSelectedDatePanel.getDateMillis() : 0;
    }

    public void setSelectedDateMillis(long dateMillis) {
        if (mSelectedDatePanel != null) {
            mSelectedDatePanel.setDateMillis(dateMillis);
        }
    }

    public void setPeriod(Period period) {
        int index = Iterables.indexOf(mPeriodsDefinition, input -> input == period);

        if (index < 0) {
            throw new IllegalArgumentException(String.format("period '%s' is not defined", period));
        }
        mPeriodSpinner.setSelection(index);
    }

    private void sendOnDateClicked(DatePanelView view) {
        mSelectedDatePanel = view;
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
