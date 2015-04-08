package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.ui.model.ActivityCalendar;
import com.simbirsoft.timemeter.ui.util.TimeUtils;

import java.util.List;

public class WeekCalendarView extends View {

    private static final int DATE_LABEL_HORIZONTAL_PADDING_DEFAULT_DIP = 12;
    private static final int DATE_LABEL_VERTICAL_PADDING_DEFAULT_DIP = 6;
    private static final int HOUR_LABEL_VERTICAL_PADDING_DEFAULT_DIP = 14;
    private static final int HOUR_LABEL_HORIZONTAL_PADDING_DEFAULT_DIP = 5;


    private ActivityCalendar mActivityCalendar;

    private int mDateLabelPaddingHorizontal;
    private int mDateLabelPaddingVertical;
    private int mHourLabelPaddingHorizontal;
    private int mHourLabelPaddingVertical;
    private int mDateWidth;
    private int mDateHeight;
    private int mHourWidth;
    private int mHourHeight;
    private Paint mDateTextPaint;
    private Paint mHourTextPaint;
    private Paint mMainLinePaint;
    private Paint mSecondaryLinePaint;

    public WeekCalendarView(Context context) {
        super(context);
    }

    public WeekCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WeekCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WeekCalendarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        final Resources res = getContext().getResources();
        final DisplayMetrics displayMetrics = res.getDisplayMetrics();

        mActivityCalendar = new ActivityCalendar();

        mDateTextPaint = new Paint();
        mDateTextPaint.setColor(res.getColor(R.color.darkGrey));
        mDateTextPaint.setTextSize(res.getDimension(R.dimen.calendar_date_text_size));
        mDateTextPaint.setAntiAlias(true);

        mHourTextPaint = new Paint();
        mHourTextPaint.setColor(res.getColor(R.color.darkGrey));
        mHourTextPaint.setTextSize(res.getDimension(R.dimen.calendar_hour_text_size));
        mHourTextPaint.setAntiAlias(true);

        mMainLinePaint = new Paint();
        mMainLinePaint.setColor(res.getColor(R.color.primary));
        mMainLinePaint.setAntiAlias(true);
        mMainLinePaint.setStrokeWidth(displayMetrics.density * 2);

        mSecondaryLinePaint = new Paint();
        mSecondaryLinePaint.setColor(res.getColor(R.color.primary));
        mSecondaryLinePaint.setAntiAlias(true);
        mSecondaryLinePaint.setStrokeWidth(1);

        mDateLabelPaddingHorizontal = (int) (displayMetrics.density * DATE_LABEL_HORIZONTAL_PADDING_DEFAULT_DIP);
        mDateLabelPaddingVertical = (int) (displayMetrics.density * DATE_LABEL_VERTICAL_PADDING_DEFAULT_DIP);
        mHourLabelPaddingHorizontal = (int) (displayMetrics.density * HOUR_LABEL_HORIZONTAL_PADDING_DEFAULT_DIP);
        mHourLabelPaddingVertical = (int) (displayMetrics.density * HOUR_LABEL_VERTICAL_PADDING_DEFAULT_DIP);
    }

    public ActivityCalendar getActivityCalendar() {
        return mActivityCalendar;
    }

    public void setActivityCalendar(ActivityCalendar activityCalendar) {
        mActivityCalendar = activityCalendar;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int myWidth = 0;
        int myHeight = 0;
        int daysCount = mActivityCalendar.getDaysCount();
        int hoursCount = mActivityCalendar.getHoursCount();

        mDateWidth = 0;
        mDateHeight = 0;
        mHourWidth = 0;
        mHourHeight = 0;

        if (daysCount > 0) {
            String dateLabelProbe = mActivityCalendar.getDateLabel(0);
            mDateWidth = (int) Math.ceil(mDateTextPaint.measureText(dateLabelProbe))
                                + mDateLabelPaddingHorizontal * 2;
            mDateHeight = (int) Math.ceil(mDateTextPaint.getTextSize() + mDateLabelPaddingVertical);
        }

        if (hoursCount > 0) {
            String hourLabelProbe = mActivityCalendar.getHourLabel(0);
            mHourWidth = (int) Math.ceil(mHourTextPaint.measureText(hourLabelProbe))
                                + mHourLabelPaddingHorizontal;
            mHourHeight = (int) Math.ceil(mHourTextPaint.getTextSize() + mHourLabelPaddingVertical * 2);
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            myWidth = MeasureSpec.getSize(widthMeasureSpec);
            if (daysCount > 0) {
                mDateWidth = Math.max((myWidth - mHourWidth) / daysCount, 0);
            }
        } else {
            myWidth = mHourWidth + daysCount * mDateWidth;
        }

        myHeight = mDateHeight + hoursCount * mHourHeight;

        setMeasuredDimension(myWidth, myHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawDates(canvas);
        drawHours(canvas);
        drawActivities(canvas);
    }

    private void drawDates(Canvas canvas) {
        int daysCount = mActivityCalendar.getDaysCount();

        if (daysCount == 0) {
            return;
        }

        final int saveCount = canvas.save();
        final int canvasHeight = canvas.getHeight();
        final Resources res = getContext().getResources();

        canvas.translate(mHourWidth, 0);
        canvas.drawLine(0, mDateHeight - 1, 0, canvasHeight, mMainLinePaint);

        for (int i = 0; i < daysCount; i++) {
            String dateText = mActivityCalendar.getDateLabel(i);
            mDateTextPaint.setColor(mActivityCalendar.getDateLabelColor(res, i));
            int width = (int) Math.ceil(mDateTextPaint.measureText(dateText));
            int offset = Math.max(0, (mDateWidth - width) / 2);
            canvas.drawText(dateText,
                    offset,
                    mDateHeight - mDateLabelPaddingVertical,
                    mDateTextPaint);
            canvas.drawLine(mDateWidth, mDateHeight, mDateWidth, canvasHeight, mSecondaryLinePaint);
            canvas.translate(mDateWidth, 0);
        }

        canvas.restoreToCount(saveCount);
    }

    private void drawHours(Canvas canvas) {
        int hoursCount = mActivityCalendar.getHoursCount();

        if (hoursCount == 0) {
            return;
        }

        final int saveCount = canvas.save();
        final int canvasWidth = canvas.getWidth();
        final int drawLabelOffset = (int) (mHourTextPaint.getTextSize() / 3f);

        canvas.translate(0, mDateHeight);
        canvas.drawLine(mHourWidth - 1, 0, canvasWidth, 0, mMainLinePaint);

        for (int i = 0; i < hoursCount; i++) {
            if (i > 0) {
                String hourText = mActivityCalendar.getHourLabel(i);
                canvas.drawText(hourText,
                        0,
                        drawLabelOffset,
                        mHourTextPaint);
            }
            canvas.drawLine(mHourWidth, mHourHeight, canvasWidth, mHourHeight, mSecondaryLinePaint);
            canvas.translate(0, mHourHeight);
        }

        canvas.restoreToCount(saveCount);
    }

    private void drawActivities(Canvas canvas) {
        int daysCount = mActivityCalendar.getDaysCount();

        if (daysCount == 0) {
            return;
        }
        final int saveCount = canvas.save();
        canvas.translate(mHourWidth, mDateHeight);
        for (int i = 0; i < daysCount; i++) {
            drawDay(canvas, i);
            canvas.translate(mDateWidth, 0);
        }
        canvas.restoreToCount(saveCount);
    }

    private void drawDay(Canvas canvas, int dayIndex) {
        List<TaskTimeSpan> spans = mActivityCalendar.getActivityForDayIndex(dayIndex);
        if (spans.isEmpty()) return;
        for (TaskTimeSpan span : spans) {
            int startY = millisToY(span.getStartTimeMillis());
            int endY = millisToY(span.getEndTimeMillis());
            if (startY == endY) continue;
            canvas.drawRect(0, startY, mDateWidth, endY, mSecondaryLinePaint);
        }
    }

    private int millisToY(long millis) {
        long offset = mActivityCalendar.getStartDate().getTime() + TimeUtils.hoursToMillis(mActivityCalendar.getHour(0));
        int y = (int)(((millis - offset)* mHourHeight) / TimeUtils.hoursToMillis(1));
        return (y < 0) ? 0 : (y > mHourHeight * mActivityCalendar.getHoursCount()) ? mHourHeight * mActivityCalendar.getHoursCount() : y;
    }
}
