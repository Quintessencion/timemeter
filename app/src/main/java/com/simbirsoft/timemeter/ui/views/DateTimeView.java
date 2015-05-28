package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.simbirsoft.timemeter.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@EViewGroup(R.layout.view_date_time)
public class DateTimeView extends RelativeLayout {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EE, dd MMM yyyy");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("kk:mm");

    @ViewById(R.id.dateText)
    EditText mDateText;

    @ViewById(R.id.timeText)
    EditText mTimeText;

    private final Calendar mCalendar = Calendar.getInstance();

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

    public long getDateTimeInMillis() {
        return mCalendar.getTimeInMillis();
    }

    public void setDateTimeInMillis(long millis) {
        mCalendar.setTimeInMillis(millis);
        update();
    }

    private void update() {
        final Date date = mCalendar.getTime();
        mDateText.setText(DATE_FORMAT.format(date));
        mTimeText.setText(TIME_FORMAT.format(date));
    }
}
