package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.gesture.GestureOverlayView;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.model.ActivityCalendar;
import com.simbirsoft.timemeter.ui.model.WeekCalendarCell;
import com.simbirsoft.timemeter.ui.util.TimeUtils;

import org.slf4j.Logger;

import java.util.List;

public class WeekCalendarView extends View implements GestureDetector.OnGestureListener {

    public interface OnCellClickListener {
        public void onCellClicked(long cellStartMillis, long offsetInCellMillis, List<TaskTimeSpan> spans);
    }

    private static final int DATE_LABEL_HORIZONTAL_PADDING_DEFAULT_DIP = 12;
    private static final int DATE_LABEL_VERTICAL_PADDING_DEFAULT_DIP = 6;
    private static final int DATE_LABELS_SPACING_DEFAULT_DIP = 2;
    private static final int HOUR_LABEL_VERTICAL_PADDING_DEFAULT_DIP = 14;
    private static final int HOUR_LABEL_HORIZONTAL_PADDING_DEFAULT_DIP = 5;
    private static final int PADDING_DEFAULT_DIP = 12;
    private static final int MAIN_LINE_WIDTH_PX = 2;
    private static final int SECONDARY_LINE_WIDTH_PX = 1;
    private static final int BLOCK_PADDING_H_PX = 2;
    private static final int BLOCK_PADDING_V_PX = 1;
    private static final int BLOCK_CORNER_RADIUS_PX = 4;
    private static final int HIGHLIGHTED_ALPHA = 150;

    private static final Logger LOG = LogFactory.getLogger(WeekCalendarView.class);
    private ActivityCalendar mActivityCalendar;

    private int mDateLabelPaddingHorizontal;
    private int mDateLabelPaddingVertical;
    private int mDateLabelsSpacing;
    private int mHourLabelPaddingHorizontal;
    private int mHourLabelPaddingVertical;
    private int mPaddingVertical;
    private int mPaddingHorizontal;
    private int mDateWidth;
    private int mDateHeight;
    private int mHourWidth;
    private int mHourHeight;
    private Paint mDateTextPaint;
    private Paint mWeekDayTextPaint;
    private Paint mHourTextPaint;
    private Paint mMainLinePaint;
    private Paint mSecondaryLinePaint;
    private Paint mTimeSpanPaint;
    private Paint mSelectionPaint;
    private OnCellClickListener mOnCellClickListener;
    private RectF mRect;
    private GestureDetector mGestureDetector;
    private WeekCalendarCell mSelectedCell;

    public WeekCalendarView(Context context) {
        super(context);
        setup();
    }

    public WeekCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public WeekCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WeekCalendarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup();
    }

    private void setup() {
        final Resources res = getContext().getResources();
        final DisplayMetrics displayMetrics = res.getDisplayMetrics();

        mActivityCalendar = new ActivityCalendar();

        mDateTextPaint = new Paint();
        mDateTextPaint.setColor(res.getColor(R.color.calendar_date_text));
        mDateTextPaint.setTextSize(res.getDimension(R.dimen.calendar_date_text_size));
        mDateTextPaint.setAntiAlias(true);

        mWeekDayTextPaint = new Paint();
        mWeekDayTextPaint.setColor(res.getColor(R.color.calendar_date_text));
        mWeekDayTextPaint.setTextSize(res.getDimension(R.dimen.calendar_day_text_size));
        mWeekDayTextPaint.setAntiAlias(true);

        mHourTextPaint = new Paint();
        mHourTextPaint.setColor(res.getColor(R.color.calendar_hour_text));
        mHourTextPaint.setTextSize(res.getDimension(R.dimen.calendar_hour_text_size));
        mHourTextPaint.setAntiAlias(true);

        mMainLinePaint = new Paint();
        mMainLinePaint.setColor(res.getColor(R.color.lightGrey));
        mMainLinePaint.setAntiAlias(true);
        mMainLinePaint.setStrokeWidth(MAIN_LINE_WIDTH_PX);

        mSecondaryLinePaint = new Paint();
        mSecondaryLinePaint.setColor(res.getColor(R.color.lightGrey));
        mSecondaryLinePaint.setAntiAlias(true);
        mSecondaryLinePaint.setStrokeWidth(SECONDARY_LINE_WIDTH_PX);

        mTimeSpanPaint = new Paint();
        mTimeSpanPaint.setAntiAlias(true);
        mTimeSpanPaint.setStrokeWidth(SECONDARY_LINE_WIDTH_PX);

        mSelectionPaint = new Paint();
        mSelectionPaint.setAntiAlias(true);
        mSelectionPaint.setStrokeWidth(MAIN_LINE_WIDTH_PX);
        mSelectionPaint.setColor(res.getColor(R.color.black));
        mSelectionPaint.setAlpha(HIGHLIGHTED_ALPHA);

        mRect = new RectF();

        mDateLabelPaddingHorizontal = (int) (displayMetrics.density * DATE_LABEL_HORIZONTAL_PADDING_DEFAULT_DIP);
        mDateLabelPaddingVertical = (int) (displayMetrics.density * DATE_LABEL_VERTICAL_PADDING_DEFAULT_DIP);
        mHourLabelPaddingHorizontal = (int) (displayMetrics.density * HOUR_LABEL_HORIZONTAL_PADDING_DEFAULT_DIP);
        mHourLabelPaddingVertical = (int) (displayMetrics.density * HOUR_LABEL_VERTICAL_PADDING_DEFAULT_DIP);
        mDateLabelsSpacing = (int) (displayMetrics.density * DATE_LABELS_SPACING_DEFAULT_DIP);
        mPaddingVertical = mPaddingHorizontal = (int) (displayMetrics.density * PADDING_DEFAULT_DIP);

        mGestureDetector = new GestureDetector(getContext(), this);
        mSelectedCell = null;
    }

    public ActivityCalendar getActivityCalendar() {
        return mActivityCalendar;
    }

    public void setActivityCalendar(ActivityCalendar activityCalendar) {
        mActivityCalendar = activityCalendar;
        mSelectedCell = null;
        requestLayout();
        invalidate();
    }

    public OnCellClickListener getOnCellClickListener() {
        return mOnCellClickListener;
    }

    public void setOnCellClickListener(OnCellClickListener listener) {
        mOnCellClickListener = listener;
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
            String dayLabelProbe = mActivityCalendar.getWeekDayLabel(0);
            mDateWidth = (int) Math.max(Math.ceil(mDateTextPaint.measureText("00")),
                                 Math.ceil(mWeekDayTextPaint.measureText(dayLabelProbe)))
                                + mDateLabelPaddingHorizontal;
            mDateHeight = (int) Math.ceil(mDateTextPaint.getTextSize() + mWeekDayTextPaint.getTextSize()
                    + mDateLabelPaddingVertical + mDateLabelsSpacing + mPaddingVertical);
        }

        if (hoursCount > 0) {
            String hourLabelProbe = mActivityCalendar.getHourLabel(0);
            mHourWidth = (int) Math.ceil(mHourTextPaint.measureText(hourLabelProbe))
                                + mHourLabelPaddingHorizontal + mPaddingHorizontal;
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

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        super.onTouchEvent(e);
        mGestureDetector.onTouchEvent(e);
        return true;
    }


    private void drawDates(Canvas canvas) {
        int daysCount = mActivityCalendar.getDaysCount();

        if (daysCount == 0) {
            return;
        }

        final int saveCount = canvas.save();
        final int canvasHeight = canvas.getHeight();
        final Resources res = getContext().getResources();
        final int weekDayTextY = mDateHeight - mDateLabelPaddingVertical;
        final int dateTextY = weekDayTextY - (int)mWeekDayTextPaint.getTextSize() - mDateLabelsSpacing;

        canvas.translate(mHourWidth, 0);
        canvas.drawLine(0, mDateHeight - 1, 0, canvasHeight, mMainLinePaint);

        for (int i = 0; i < daysCount; i++) {
            String weekDayText = mActivityCalendar.getWeekDayLabel(i);
            String dateText = mActivityCalendar.getDateLabel(i);
            mDateTextPaint.setColor(mActivityCalendar.getDateLabelColor(res, i));
            mWeekDayTextPaint.setColor(mActivityCalendar.getDateLabelColor(res, i));
            canvas.drawText(dateText,
                    0,
                    dateTextY,
                    mDateTextPaint);
            canvas.drawText(weekDayText,
                    0,
                    weekDayTextY,
                    mWeekDayTextPaint);
            if (i < daysCount - 1) {
                canvas.drawLine(mDateWidth, mDateHeight, mDateWidth, canvasHeight, mSecondaryLinePaint);
            }
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
                        mPaddingHorizontal,
                        drawLabelOffset,
                        mHourTextPaint);
            }
            if (i < hoursCount - 1) {
                canvas.drawLine(mHourWidth, mHourHeight, canvasWidth, mHourHeight, mSecondaryLinePaint);
            }
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
        long dayStart = mActivityCalendar.getDayStartMillis(dayIndex);
        List<TaskTimeSpan> selectedSpans = (mSelectedCell != null && mSelectedCell.getDayIndex() == dayIndex)
                ? mActivityCalendar.getActivitiesInCell(mSelectedCell) : null;
        for (TaskTimeSpan span : spans) {
            int startY = millisToY(span.getStartTimeMillis(), dayStart);
            startY += ((startY == 0) ? MAIN_LINE_WIDTH_PX : 0) + BLOCK_PADDING_V_PX;
            int endY = millisToY(span.getEndTimeMillis(), dayStart) - BLOCK_PADDING_V_PX;
            int startX = ((dayIndex == 0) ? MAIN_LINE_WIDTH_PX  : SECONDARY_LINE_WIDTH_PX) + BLOCK_PADDING_H_PX;
            int endX = mDateWidth - BLOCK_PADDING_H_PX - SECONDARY_LINE_WIDTH_PX;
            mTimeSpanPaint.setColor(mActivityCalendar.getTimeSpanColor(span));
            drawSpan(canvas, startX, startY, endX, endY, mTimeSpanPaint);
            if (selectedSpans != null && selectedSpans.contains(span)) {
                drawSpan(canvas, startX, startY, endX, endY, mSelectionPaint);
            }
        }
    }

    private void drawSpan(Canvas canvas, int startX, int startY, int endX, int endY, Paint paint) {
        if (startY > endY) {
            canvas.drawLine(startX, startY, endX, startY, paint);
        } else {
            mRect.set(startX, startY, endX, endY);
            canvas.drawRoundRect(mRect, BLOCK_CORNER_RADIUS_PX, BLOCK_CORNER_RADIUS_PX, paint);
        }
    }

    private int millisToY(long millis, long dayStart) {
        long offset = dayStart + TimeUtils.hoursToMillis(mActivityCalendar.getHour(0));
        int y = (int)(((millis - offset)* mHourHeight) / TimeUtils.MILLIS_IN_HOUR);
        int maxY = mHourHeight * mActivityCalendar.getHoursCount();
        return Math.max(0, Math.min(y, maxY));
    }

    private WeekCalendarCell getCell(MotionEvent e) {
        int x = (int)e.getX() - mHourWidth;
        int y = (int)e.getY() - mDateHeight;
        if (x < 0 || y < 0 || mDateWidth == 0 || mHourHeight == 0) return null;
        return new WeekCalendarCell(x / mDateWidth, y / mHourHeight);
    }

    public boolean onSingleTapUp(MotionEvent e) {
        WeekCalendarCell prevCell = mSelectedCell;
        mSelectedCell = getCell(e);
        if (!(prevCell != null && mSelectedCell != null && mSelectedCell.equals(prevCell))) {
            invalidate();
        }
        return true;
    }

    public void onLongPress(MotionEvent e) {
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2,
                            float distanceX, float distanceY) {
        return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        return false;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onDown(MotionEvent e) {
        return false;
    }
}
