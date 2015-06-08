package com.simbirsoft.timemeter.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.github.mikephil.charting.utils.Legend;
import com.simbirsoft.timemeter.R;

public class VerticalLegend extends LinearLayout implements View.OnClickListener {
    private static final String TEXT_COLOR = "black";

    public interface ClickListener {
        void onLabelClicked(int position);
    }

    private ClickListener mClickListener;

    private Legend mLegend;

    private boolean mIsClickable = true;

    public VerticalLegend(Context context) {
        super(context);
    }

    public VerticalLegend(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalLegend(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setLegend(Legend legend) {
        mLegend = legend;
    }

    public void setClickListener(ClickListener clickListener) {
        mClickListener = clickListener;
    }

    public void update() {
        removeAllViews();
        setOrientation(VERTICAL);

        String[] labels = mLegend.getLegendLabels();
        int[] colors = mLegend.getColors();

        for (int i = 0; i < labels.length; i++) {
            addView(getLegendItem(colors[i], labels[i], i));
        }

        invalidate();
    }

    @Override
    public void onClick(View v) {
        if (mClickListener != null) {
            int position = (Integer)v.getTag();
            mClickListener.onLabelClicked(position);
        }
    }

    private FrameLayout getLegendItem(int color, String text, int position) {
        MaterialRippleLayout materialRippleLayout = new MaterialRippleLayout(getContext());
        FrameLayout.LayoutParams materialRippleLayoutParams =
                new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        int padding = (mIsClickable)?
                getContext().getResources().getDimensionPixelSize(R.dimen.chart_legend_labels_padding_big):
                getContext().getResources().getDimensionPixelSize(R.dimen.chart_legend_labels_padding_normal);

        materialRippleLayout.setPadding(0, padding, 0, padding);
        materialRippleLayout.setLayoutParams(materialRippleLayoutParams);

        LinearLayout linearLayout = getContainerLayout(position);

        linearLayout.addView(getCircle(color));
        linearLayout.addView(getText(text));

        materialRippleLayout.addView(linearLayout);
        materialRippleLayout.setEnabled(mIsClickable);
        materialRippleLayout.setFocusable(mIsClickable);

        if (mIsClickable) {
            materialRippleLayout.setOnClickListener(this);
        }

        return materialRippleLayout;
    }

    private LinearLayout getContainerLayout(int position) {
        LinearLayout linearLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = (int) mLegend.getYEntrySpace();
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(HORIZONTAL);
        linearLayout.setTag(position);
        return linearLayout;
    }

    private View getCircle(int color) {
        View imageView = new View(getContext());
        imageView.setLayoutParams(getImageParams());
        imageView.setBackgroundResource(R.drawable.task_marker);

        GradientDrawable drawable = (GradientDrawable)imageView.getBackground();
        drawable.setColor(color);

        return imageView;
    }

    private TextView getText(String text) {
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(getTextParams());
        textView.setText(text);
        textView.setTextColor(Color.parseColor(TEXT_COLOR));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.chart_legend_label_text_size));
        textView.setEnabled(mIsClickable);
        textView.setFocusable(mIsClickable);
        return textView;
    }

    private LinearLayout.LayoutParams getImageParams() {
        final int circleDiameter = (int) getResources().getDimension(R.dimen.task_marker_size);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(circleDiameter, circleDiameter);
        params.gravity = Gravity.CENTER_VERTICAL;
        return params;
    }

    private LinearLayout.LayoutParams getTextParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.leftMargin = (int)mLegend.getXEntrySpace();
        return params;
    }

    public void setIsClickable(boolean isClickable) {
        mIsClickable = isClickable;
    }
}
