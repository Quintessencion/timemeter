package com.simbirsoft.timeactivity.ui.views;

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

import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.ui.model.TaskActivitySpansItem;

public class TaskActivityItemView extends View{
    private static final int BLOCK_HORIZONTAL_PADDING_DEFAULT_DIP = 10;
    private static final int BLOCK_VERTICAL_PADDING_DEFAULT_DIP = 5;
    private static final int BLOCK_HORIZONTAL_SPACING_DEFAULT_DIP = 10;
    private static final int BLOCK_CORNER_RADIUS_DEFAULT_DIP = 4;

    private Paint mTimeTextPaint;
    private Paint mDurationTextPaint;
    private Paint mBlockPaint;

    private int mBlockHorizontalPadding;
    private int mBlockVerticalPadding;
    private int mTimeBlockWidth;
    private int mBlockHeight;
    private int mBlockHorizontalSpacing;
    private int mBlockCornerRadius;
    private RectF mRect;
    private Rect mTextBounds;
    private Context mContext;
    private int mTimeBlockColor;
    private int mDurationBlockColor;
    private int mTimeBlockHighlightedColor;
    private int mDurationBlockHighlightedColor;
    private int mTimeBlockSelectedColor;
    private int mDurationBlockSelectedColor;
    private int mTimeBlockAccentSelectedColor;
    private int mDurationBlockAccentSelectedColor;
    private boolean mIsHighlighted;
    private boolean mIsSelected;

    private TaskActivitySpansItem mItem;
    private int mIndex;

    public TaskActivityItemView(Context context) {
        super(context);
    }

    public TaskActivityItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TaskActivityItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TaskActivityItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContext = getContext();
        final Resources res = mContext.getResources();
        final DisplayMetrics displayMetrics = res.getDisplayMetrics();

        mTimeTextPaint = new Paint();
        mTimeTextPaint.setColor(res.getColor(R.color.white));
        mTimeTextPaint.setTextSize(res.getDimension(R.dimen.task_activity_time_text_size));
        mTimeTextPaint.setAntiAlias(true);

        mDurationTextPaint = new Paint();
        mDurationTextPaint.setColor(res.getColor(R.color.white));
        mDurationTextPaint.setTextSize(res.getDimension(R.dimen.task_activity_time_text_size));
        mDurationTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mDurationTextPaint.setAntiAlias(true);

        mBlockPaint = new Paint();
        mBlockPaint.setAntiAlias(true);

        mBlockHorizontalPadding = (int) (displayMetrics.density * BLOCK_HORIZONTAL_PADDING_DEFAULT_DIP);
        mBlockVerticalPadding = (int) (displayMetrics.density * BLOCK_VERTICAL_PADDING_DEFAULT_DIP);
        mBlockHorizontalSpacing = (int) (displayMetrics.density * BLOCK_HORIZONTAL_SPACING_DEFAULT_DIP);
        mBlockCornerRadius = (int) (displayMetrics.density * BLOCK_CORNER_RADIUS_DEFAULT_DIP);

        mTimeBlockColor = res.getColor(R.color.primary);
        mDurationBlockColor = res.getColor(R.color.primaryDark);
        mTimeBlockHighlightedColor = res.getColor(R.color.accentPrimary);
        mDurationBlockHighlightedColor = res.getColor(R.color.accentDark);
        mTimeBlockSelectedColor = res.getColor(R.color.accentLight);
        mDurationBlockSelectedColor = res.getColor(R.color.accentLight);
        mTimeBlockAccentSelectedColor = res.getColor(R.color.accentLight);
        mDurationBlockAccentSelectedColor = res.getColor(R.color.accentLight);

        mIndex = -1;
        mItem = new TaskActivitySpansItem();
        mRect = new RectF();
        mTextBounds = new Rect();
    }

    public void setTaskActivitySpansItem(TaskActivitySpansItem item, int index, boolean isHighlighted, boolean isSelected) {
        mItem = item;
        mIndex = index;
        mIsHighlighted = isHighlighted;
        mIsSelected = isSelected;
        invalidate();
    }

    public TaskActivitySpansItem getItem() {
        return mItem;
    }

    public int getIndex() {
        return mIndex;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int myWidth = 0;
        mBlockHeight = (int)Math.ceil(Math.max(mTimeTextPaint.getTextSize(), mDurationTextPaint.getTextSize()))
                + 2 * mBlockVerticalPadding;
        String text = (mIndex < 0) ? mItem.getSpanTimeTestLabel() : mItem.getSpanTimeLabel(mIndex);
        mTimeBlockWidth = (int)Math.ceil(mTimeTextPaint.measureText(text)) + 2 * mBlockHorizontalPadding;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            myWidth = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            text = (mIndex < 0) ? mItem.getSpanDurationTestLabel(mContext) : mItem.getSpanDurationLabel(mIndex, mContext);
            int durationBlockWidth = (int)Math.ceil(mDurationTextPaint.measureText(text)) + 2 * mBlockHorizontalPadding;
            myWidth = mTimeBlockWidth + durationBlockWidth + mBlockHorizontalSpacing;
        }

        setMeasuredDimension(myWidth, mBlockHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mIndex < 0) return;
        final int saveCount = canvas.save();
        int timeBlockColor;
        int durationBlockColor;
        if (mIsSelected) {
            timeBlockColor = mIsHighlighted ? mTimeBlockAccentSelectedColor : mTimeBlockSelectedColor;
            durationBlockColor = mIsHighlighted ? mDurationBlockAccentSelectedColor : mDurationBlockSelectedColor;
        } else {
            timeBlockColor = mIsHighlighted ? mTimeBlockHighlightedColor : mTimeBlockColor;
            durationBlockColor = mIsHighlighted ? mDurationBlockHighlightedColor : mDurationBlockColor;
        }
        mBlockPaint.setColor(timeBlockColor);
        drawBlock(canvas, mItem.getSpanTimeLabel(mIndex), mTimeBlockWidth, mTimeTextPaint);
        canvas.translate(mTimeBlockWidth + mBlockHorizontalSpacing, 0);
        mBlockPaint.setColor(durationBlockColor);
        drawBlock(canvas, mItem.getSpanDurationLabel(mIndex, mContext), 0, mDurationTextPaint);
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
