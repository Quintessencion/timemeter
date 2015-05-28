package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.util.TimeUtils;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@EViewGroup(R.layout.view_date_time)
public class DateTimeView extends RelativeLayout {
    public interface DateTimeViewListener {
        void onDateTextClicked(DateTimeView v, Calendar selectedDate);
        void onTimeTextClicked(DateTimeView v, Calendar selectedTime);
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EE, dd MMM yyyy");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("kk:mm");

    @ViewById(R.id.dateText)
    EditText mDateText;

    @ViewById(R.id.timeText)
    EditText mTimeText;

    private final Calendar mCalendar = Calendar.getInstance();
    private final Calendar mBufferCalendar = Calendar.getInstance();
    private DateTimeViewListener mDateTimeViewListener;

    public DateTimeView(Context context) {
        super(context);
    }

    public DateTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DateTimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DateTimeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Click(R.id.dateText)
    void onDateTextClicked(View v) {
        if (mDateTimeViewListener != null) {
            mBufferCalendar.setTimeInMillis(mCalendar.getTimeInMillis());
            mDateTimeViewListener.onDateTextClicked(this, mBufferCalendar);
        }
    }

    @Click(R.id.timeText)
    void onTimeTextClicked(View v) {
        if (mDateTimeViewListener != null) {
            mBufferCalendar.setTimeInMillis(mCalendar.getTimeInMillis());
            mDateTimeViewListener.onTimeTextClicked(this, mBufferCalendar);
        }
    }

    public void setDateTimeViewListener(DateTimeViewListener вateTimeViewListener) {
        mDateTimeViewListener = вateTimeViewListener;
    }

    public long getDateTimeInMillis() {
        return mCalendar.getTimeInMillis();
    }

    public void setDateTimeInMillis(long millis) {
        mCalendar.setTimeInMillis(millis);
        update();
    }

    public long getTimeValueInMillis() {
        mBufferCalendar.setTimeInMillis(mCalendar.getTimeInMillis());
        mBufferCalendar.setTimeInMillis(TimeUtils.getDayStartMillis(mBufferCalendar));
        return (mCalendar.getTimeInMillis() - mBufferCalendar.getTimeInMillis());
    }

    public long getDateValueInMillis() {
        mBufferCalendar.setTimeInMillis(mCalendar.getTimeInMillis());
        mBufferCalendar.setTimeInMillis(TimeUtils.getDayStartMillis(mBufferCalendar));
        return mBufferCalendar.getTimeInMillis();
    }

    private void update() {
        final Date date = mCalendar.getTime();
        mDateText.setText(DATE_FORMAT.format(date));
        mTimeText.setText(TIME_FORMAT.format(date));
    }
}
