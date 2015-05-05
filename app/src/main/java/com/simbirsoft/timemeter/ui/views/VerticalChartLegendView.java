package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.Utils;

public class VerticalChartLegendView extends View {

    private Legend mLegend;
    private Paint mLegendLabelPaint;
    private Paint mLegendFormPaint;
    private int mTextHeight;

    public VerticalChartLegendView(Context context) {
        super(context);
    }

    public VerticalChartLegendView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalChartLegendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VerticalChartLegendView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mLegendLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLegendLabelPaint.setTextSize(Utils.convertDpToPixel(9f));
        mLegendLabelPaint.setTextAlign(Paint.Align.LEFT);

        mLegendFormPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLegendFormPaint.setStyle(Paint.Style.FILL);
        mLegendFormPaint.setStrokeWidth(3f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mLegend == null) {
            setMeasuredDimension(0, 0);
            return;
        }

        mTextHeight = Utils.calcTextHeight(mLegendLabelPaint, "DEMO TEXT");

        final int labelCount = mLegend.getLegendLabels().length;
        int height = mTextHeight * labelCount + getPaddingTop();
        if (labelCount > 0) {
            height += (labelCount - 1) * (int) mLegend.getYEntrySpace();
        }

        int width = mLegend.getMaximumEntryLength(mLegendLabelPaint);
        int desiredWidth = MeasureSpec.getSize(widthMeasureSpec);
        width = Math.max(width, desiredWidth);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mLegend == null) {
            return;
        }

        final int count = mLegend.getLegendLabels().length;

        if (count == 0) {
            return;
        }

        final float formSize = mLegend.getFormSize();
        final int formTextMargin = (int) (mLegend.getFormToTextSpace() + formSize);
        final int formYOffset = (int) ((mTextHeight / 2) + formSize / 2);
        final int formEntrySpace = (int) mLegend.getYEntrySpace();

        canvas.translate(getPaddingLeft(), getPaddingTop() + mTextHeight);

        for (int i = 0; i < count; i++) {
            mLegend.drawForm(canvas,
                    0,
                    -formYOffset,
                    mLegendFormPaint,
                    i);

            mLegend.drawLabel(canvas,
                    formTextMargin,
                    0,
                    mLegendLabelPaint,
                    i);

            canvas.translate(0, mTextHeight + formEntrySpace);
        }
    }

    public Legend getLegend() {
        return mLegend;
    }

    public void setLegend(Legend legend) {
        mLegend = legend;

        if (mLegend != null) {
            mLegendLabelPaint.setTextSize(mLegend.getTextSize());
            mLegendLabelPaint.setColor(mLegend.getTextColor());
        }

        invalidate();
    }
}
