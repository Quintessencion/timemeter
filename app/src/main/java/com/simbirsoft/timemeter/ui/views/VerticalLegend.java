package com.simbirsoft.timemeter.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.utils.Legend;
import com.pkmmte.view.CircularImageView;

public class VerticalLegend extends LinearLayout implements View.OnClickListener {

    public interface LegendClickListener {
        void onLabelClicked(int position);
    }

    private LegendClickListener mLegendClickListener;

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

    public void setLegendClickListener(LegendClickListener legendClickListener) {
        mLegendClickListener = legendClickListener;
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
        if (mLegendClickListener != null) {
            int position = (Integer)v.getTag();
            mLegendClickListener.onLabelClicked(position);
        }
    }

    private LinearLayout getLegendItem(int color, String text, int position) {
        LinearLayout linearLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = (int)mLegend.getYEntrySpace() / 2;
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(HORIZONTAL);

        LinearLayout.LayoutParams paramsImage = getImageParams();
        CircularImageView circle = getCircle(color);
        linearLayout.addView(circle, paramsImage);

        LinearLayout.LayoutParams paramsText = getTextParams();
        TextView textView = getText(text, position);
        linearLayout.addView(textView, paramsText);

        return linearLayout;
    }

    private CircularImageView getCircle(int color) {
        CircularImageView imageView = new CircularImageView(getContext());
        imageView.setBackgroundColor(color);
        return imageView;
    }

    private TextView getText(String text, int position) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTag(position);
        textView.setTextColor(Color.parseColor("black"));
        textView.setTextSize(mLegend.getTextSize() / 2);
        textView.setEnabled(mIsClickable);
        textView.setFocusable(mIsClickable);
        if (mIsClickable) {
            textView.setOnClickListener(this);
        }
        return textView;
    }

    private LinearLayout.LayoutParams getImageParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
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
