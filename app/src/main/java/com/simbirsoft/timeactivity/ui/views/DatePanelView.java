package com.simbirsoft.timeactivity.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.simbirsoft.timeactivity.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;

@EViewGroup(R.layout.view_date_panel)
public class DatePanelView extends LinearLayout{

    interface DatePanelViewListener {
        void onDateTextClicked();
        void onDateReset();
    }

    @ViewById(R.id.dateText)
    TextView mDateText;

    private long mDateMillis;
    private DatePanelViewListener mDatePanelViewListener;

    public DatePanelView(Context context) {
        super(context);
    }

    public DatePanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DatePanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DatePanelView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @AfterViews
    void initializeView() {
        printDate();
    }

    @Click(R.id.dateText)
    void onDateTextClicked(View v) {
        if (mDatePanelViewListener != null) {
            mDatePanelViewListener.onDateTextClicked();
        }
    }

    @Click(R.id.buttonResetDate)
    void onResetDateClicked(View v) {
        mDateMillis = 0;
        if (mDatePanelViewListener != null) {
            mDatePanelViewListener.onDateReset();
        }
    }

    public void setDatePanelViewListener(DatePanelViewListener datePanelViewListener) {
        mDatePanelViewListener = datePanelViewListener;
    }

    public long getDateMillis() {
        return mDateMillis;
    }

    public void updateView() {
        printDate();
    }

    public void setDateMillis(long dateMillis) {
        mDateMillis = dateMillis;
        printDate();
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
}
