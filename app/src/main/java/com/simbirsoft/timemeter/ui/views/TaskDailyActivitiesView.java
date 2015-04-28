package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.model.TaskActivitySpansItem;

public class TaskDailyActivitiesView extends View{
    private static final int BLOCK_HORIZONTAL_PADDING_DEFAULT_DIP = 10;
    private static final int BLOCK_VERTICAL_PADDING_DEFAULT_DIP = 5;
    private static final int BLOCK_VERTICAL_SPACING_DEFAULT_DIP = 10;
    private static final int BLOCK_HORIZONTAL_SPACING_DEFAULT_DIP = 15;
    private static final int BLOCK_CORNER_RADIUS_DEFAULT_DIP = 4;

    private Paint mTimeTextPaint;
    private Paint mDurationTextPaint;
    private Paint mBlockPaint;

    private int mBlockHorizontalPadding;
    private int mBlockVerticalPadding;
    private int mTimeBlockWidth;
    private int mBlockHeight;
    private int mBlockVerticalSpacing;
    private int mBlockHorizontalSpacing;
    private int mBlockCornerRadius;
    private RectF mRect;
    private Rect mTextBounds;

    private TaskActivitySpansItem mItem;

    public TaskDailyActivitiesView(Context context) {
        super(context);
    }

    public TaskDailyActivitiesView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TaskDailyActivitiesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TaskDailyActivitiesView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final Resources res = getContext().getResources();
        final DisplayMetrics displayMetrics = res.getDisplayMetrics();
        mTimeTextPaint = new Paint();
        mTimeTextPaint.setColor(res.getColor(R.color.white));
        mTimeTextPaint.setTextSize(res.getDimension(R.dimen.task_activity_text_size));
        mTimeTextPaint.setAntiAlias(true);

        mDurationTextPaint = new Paint();
        mDurationTextPaint.setColor(res.getColor(R.color.white));
        mDurationTextPaint.setTextSize(res.getDimension(R.dimen.task_activity_text_size));
        mDurationTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mDurationTextPaint.setAntiAlias(true);

        mBlockPaint = new Paint();
        mBlockPaint.setAntiAlias(true);

        mBlockHorizontalPadding = (int) (displayMetrics.density * BLOCK_HORIZONTAL_PADDING_DEFAULT_DIP);
        mBlockVerticalPadding = (int) (displayMetrics.density * BLOCK_VERTICAL_PADDING_DEFAULT_DIP);
        mBlockVerticalSpacing = (int) (displayMetrics.density * BLOCK_VERTICAL_SPACING_DEFAULT_DIP);
        mBlockHorizontalSpacing = (int) (displayMetrics.density * BLOCK_HORIZONTAL_SPACING_DEFAULT_DIP);
        mBlockCornerRadius = (int) (displayMetrics.density * BLOCK_CORNER_RADIUS_DEFAULT_DIP);

        mItem = new TaskActivitySpansItem();
        mRect = new RectF();
        mTextBounds = new Rect();
    }

    public void setTaskActivitySpansItem(TaskActivitySpansItem item) {
        mItem = item;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int myWidth = 0;
        int myHeight = 0;
        mBlockHeight = (int)Math.ceil(Math.max(mTimeTextPaint.getTextSize(), mDurationTextPaint.getTextSize()))
                + 2 * mBlockVerticalPadding;
        String testString = mItem.getSpanTimeTestLabel();
        mTimeBlockWidth = (int)Math.ceil(mTimeTextPaint.measureText(testString)) + 2 * mBlockHorizontalPadding;
        int count = mItem.getSpansCount();
        myHeight = Math.max(0, count * mBlockHeight + (count - 1) * mBlockVerticalSpacing);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            myWidth = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            testString = mItem.getSpanDurationTestLabel(getContext());
            int durationBlockWidth = (int)Math.ceil(mDurationTextPaint.measureText(testString)) + 2 * mBlockHorizontalPadding;
            myWidth = mTimeBlockWidth + durationBlockWidth + mBlockHorizontalSpacing;
        }

        setMeasuredDimension(myWidth, myHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int count = mItem.getSpansCount();
        if (count == 0) return;
        final int saveCount = canvas.save();
        for (int i = 0; i < count; i++) {
            drawItem(canvas, i);
            canvas.translate(0, mBlockHeight + mBlockVerticalSpacing);
        }
        canvas.restoreToCount(saveCount);
    }

    private void drawItem(Canvas canvas, int index) {
        final Resources res = getContext().getResources();
        final int saveCount = canvas.save();
        mBlockPaint.setColor(res.getColor(R.color.primary));
        drawBlock(canvas, mItem.getSpanTimeLabel(index), mTimeBlockWidth, mTimeTextPaint);
        canvas.translate(mTimeBlockWidth + mBlockHorizontalSpacing, 0);
        mBlockPaint.setColor(res.getColor(R.color.primaryDark));
        drawBlock(canvas, mItem.getSpanDurationLabel(index, getContext()), 0, mDurationTextPaint);
        canvas.restoreToCount(saveCount);
    }

    private void drawBlock(Canvas canvas, String text, int blockWidth, Paint textPaint) {
        textPaint.getTextBounds(text, 0, text.length(), mTextBounds);
        int width = (blockWidth == 0)
                ? (mTextBounds.right - mTextBounds.left) + 2 * mBlockHorizontalPadding
                : blockWidth;
        mRect.set(0, 0, width, mBlockHeight);
        canvas.drawRoundRect(mRect, mBlockCornerRadius, mBlockCornerRadius, mBlockPaint);
        int offset = (mBlockHeight - (mTextBounds.bottom - mTextBounds.top)) / 2;
        canvas.drawText(text, mBlockHorizontalPadding - mTextBounds.left, mBlockHeight - offset - mTextBounds.bottom, textPaint);
    }
}
