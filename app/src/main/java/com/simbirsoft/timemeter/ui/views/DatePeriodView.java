package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.model.Period;
import com.simbirsoft.timemeter.ui.util.TagViewUtils;
import com.simbirsoft.timemeter.ui.util.ToastUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.androidannotations.annotations.res.StringRes;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Nullable;

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

    @ViewById(R.id.taggedPanel)
    ViewGroup mTaggedPanel;

    @ViewById(R.id.dateText)
    TextView mDateText;

    @ViewById(R.id.periodSpinner)
    Spinner mPeriodSpinner;

    @StringRes(R.string.hint_choose_filter_period)
    String mHintChooseFilterPeriod;

    private ArrayAdapter<CharSequence> mAdapter;
    private long mDateMillis;
    private DatePeriodViewListener mDatePeriodViewListener;
    private List<String> mPeriodList;
    private int mItemsSrc;

    @LongClick(R.id.periodSpinner)
    void onPeriodSpinnerLongClicked(View v) {
        ToastUtils.showToastWithAnchor(getContext(),
                mHintChooseFilterPeriod, v, Toast.LENGTH_SHORT);
    }

    @Click(R.id.dateText)
    void onDateTextClicked(View v) {
        LOG.debug("date text clicked");
        if (mDatePeriodViewListener != null) {
            mDatePeriodViewListener.onDateTextClicked();
        }
    }

    @Click(R.id.buttonResetDate)
    void onResetDateClicked(View v) {
        LOG.debug("reset date period clicked");
        mDateMillis = 0;
        if (mDatePeriodViewListener != null) {
            mDatePeriodViewListener.onDateReset();
        }
    }

    @ItemSelect(R.id.periodSpinner)
    void onPeriodSelected(boolean selected, CharSequence selectedPeriod) {
        if (selected) {
            LOG.info("selected period: {}", selectedPeriod);
            if (mDatePeriodViewListener != null) {
                mDatePeriodViewListener.onPeriodSelected(getPeriod());
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
        TagViewUtils.updateTagViewColor(mTaggedPanel, tagColor);
        printDate();
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
        state.putLong(EXTRA_DATE_MILLIS, mDateMillis);

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(((Bundle) state).getParcelable(EXTRA_SUPER_STATE));

        mDateMillis = ((Bundle) state).getLong(EXTRA_DATE_MILLIS);
    }

    public long getDateMillis() {
        return mDateMillis;
    }

    public void setDateMillis(long dateMillis) {
        mDateMillis = dateMillis;

        printDate();
    }
    public void setPeriod(Period period) {
        int index = Iterables.indexOf(mPeriodsDefinition, input -> input == period);

        if (index < 0) {
            throw new IllegalArgumentException(String.format("period '%s' is not defined", period));
        }

        mPeriodSpinner.setSelection(index);
    }

    private void printDate() {
        if (mDateMillis == 0) {
            mDateText.setText("");
            return;
        }

        Calendar today = Calendar.getInstance();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mDateMillis);

        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {

            mDateText.setText(getContext().getString(R.string.caption_today));

        } else if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            mDateText.setText(DateFormat.format("dd MMM", mDateMillis));

        } else {
            mDateText.setText(DateFormat.format("dd MMM yyyy", mDateMillis));
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
