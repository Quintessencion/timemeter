package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.Utils;

public class VerticalChartLegendView extends View {

    private Legend mLegend;
    private Paint mLegendLabelPaint;
    private Paint mLegendFormPaint;

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

        final int labelCount = mLegend.getLegendLabels().length;
        int height = (int) Math.ceil(mLegend.getFullHeight(mLegendLabelPaint))
                + getPaddingTop()
                + (int) (mLegend.getStackSpace() * (labelCount - 1));

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

        final float stackSpace = mLegend.getStackSpace();
        final float formSize = mLegend.getFormSize();
        final float formTextSpaceAndForm = mLegend.getFormToTextSpace() + formSize;
        final float formDrawOffset = formSize / 4;

        int x = getPaddingLeft();
        int y = getPaddingTop();
        for (int i = 0; i < count; i++) {
            String label = mLegend.getLegendLabels()[i];

            float textSize;
            if (label != null) {
                textSize = Utils.calcTextHeight(mLegendLabelPaint, label);

                mLegend.drawForm(canvas,
                        x,
                        y + (textSize / 2) - formDrawOffset,
                        mLegendFormPaint,
                        i);

                mLegend.drawLabel(canvas,
                        x + formTextSpaceAndForm,
                        y + textSize,
                        mLegendLabelPaint,
                        i);
            } else {
                textSize = mLegend.getTextSize();
            }

            y += textSize + stackSpace + mLegend.getYEntrySpace();
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
