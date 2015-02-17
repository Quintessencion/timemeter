package com.simbirsoft.timemeter.ui.stats;

import android.content.Context;
import android.text.Html;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.MarkerView;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.util.TimerTextFormatter;

public class OverallTaskActivityChartMarkerView extends MarkerView {

    private TextView mTitleView;
    private TextView mSubtitleView;

    public OverallTaskActivityChartMarkerView(Context context) {
        super(context, R.layout.view_overall_activity_chart_marker_view);

        mTitleView = (TextView) findViewById(android.R.id.title);
        mSubtitleView = (TextView) findViewById(R.id.subtitle);
    }

    @Override
    public void refreshContent(Entry e, int dataSetIndex) {
        String timeText = TimerTextFormatter.formatTaskTimerText(
                mTitleView.getResources(), (int) e.getVal());
        mTitleView.setText((String) e.getData());
        mSubtitleView.setText(Html.fromHtml(timeText));
    }

    @Override
    public int getXOffset() {
        return -(getWidth() / 2);
    }

    @Override
    public int getYOffset() {
        return -getHeight();
    }
}
