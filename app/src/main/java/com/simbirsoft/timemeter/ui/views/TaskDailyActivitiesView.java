package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.model.TaskActivitySpansItem;

import java.util.logging.Logger;

public class TaskDailyActivitiesView extends View{
    private static final int BLOCK_HORIZONTAL_PADDING_DEFAULT_DIP = 10;
    private static final int BLOCK_VERTICAL_PADDING_DEFAULT_DIP = 4;
    private static final int DASH_STROKE_DEFAULT_PX = 2;
    private static final int DASH_PADDING_DEFAULT_DIP = 5;
    private static final int DASH_WIDTH_DEFAULT_DIP = 10;
    private static final int BLOCK_SPACING_DEFAULT_DIP = 10;
    private static final int BLOCK_CORNER_RADIUS_DEFAULT_DIP = 10;

    private Paint mTextPaint;
    private Paint mBlockPaint;

    private int mBlockHorizontalPadding;
    private int mBlockVerticalPadding;
    private int mDashWidth;
    private int mDashPadding;
    private int mBlockWidth;
    private int mBlockHeight;
    private int mBlockSpacing;
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
        mTextPaint = new Paint();
        mTextPaint.setColor(res.getColor(R.color.white));
        mTextPaint.setTextSize(res.getDimension(R.dimen.task_activity_text_size));
        mTextPaint.setAntiAlias(true);

        mBlockPaint = new Paint();
        mBlockPaint.setColor(res.getColor(R.color.primary));
        mBlockPaint.setStrokeWidth(DASH_STROKE_DEFAULT_PX);
        mBlockPaint.setAntiAlias(true);

        mBlockHorizontalPadding = (int) (displayMetrics.density * BLOCK_HORIZONTAL_PADDING_DEFAULT_DIP);
        mBlockVerticalPadding = (int) (displayMetrics.density * BLOCK_VERTICAL_PADDING_DEFAULT_DIP);
        mDashWidth = (int) (displayMetrics.density * DASH_WIDTH_DEFAULT_DIP);
        mDashPadding = (int) (displayMetrics.density * DASH_PADDING_DEFAULT_DIP);
        mBlockSpacing = (int) (displayMetrics.density * BLOCK_SPACING_DEFAULT_DIP);
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
        mTextPaint.getTextBounds(TaskActivitySpansItem.TEST_STRING,0, TaskActivitySpansItem.TEST_STRING.length(), mTextBounds);
        mBlockHeight = mTextBounds.bottom - mTextBounds.top + 2 * mBlockVerticalPadding;
        mBlockWidth  = mTextBounds.right - mTextBounds.left + 2 * mBlockHorizontalPadding;

        int count = mItem.getSpansCount();
        myHeight = Math.max(0, count * mBlockHeight + (count - 1) * mBlockSpacing);
        myWidth = 2 * (mBlockWidth + mDashPadding) + mDashWidth;

       setMeasuredDimension(myWidth, myHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int count = mItem.getSpansCount();
        if (count == 0) return;
        final int saveCount = canvas.save();
        for (int i = 0; i < count; i++) {
            drawItem(canvas, i);
            canvas.translate(0, mBlockHeight + mBlockSpacing);
        }
        canvas.restoreToCount(saveCount);
    }

    private void drawItem(Canvas canvas, int index) {
        final int saveCount = canvas.save();
        drawBlock(canvas, mItem.getStartHourLabel(index));
        canvas.translate(mBlockWidth + mDashPadding, 0);
        canvas.drawLine(0, mBlockHeight/2, mDashWidth, mBlockHeight/2, mBlockPaint);
        canvas.translate(mDashWidth + mDashPadding, 0);
        drawBlock(canvas, mItem.getEndHourLabel(index));
        canvas.restoreToCount(saveCount);
    }

    private void drawBlock(Canvas canvas, String text) {
        mRect.set(0, 0, mBlockWidth, mBlockHeight);
        canvas.drawRoundRect(mRect, mBlockCornerRadius, mBlockCornerRadius, mBlockPaint);
        Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
        canvas.drawText(text, mBlockHorizontalPadding - mTextBounds.left, mBlockHeight - mBlockVerticalPadding - mTextBounds.bottom, mTextPaint);
    }
}
