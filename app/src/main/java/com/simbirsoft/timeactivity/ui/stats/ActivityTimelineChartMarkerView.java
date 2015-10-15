package com.simbirsoft.timeactivity.ui.stats;

import android.content.Context;
import android.text.Html;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.MarkerView;
import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.ui.util.TimerTextFormatter;

public class ActivityTimelineChartMarkerView extends MarkerView {

    private TextView mTitleView;

    public ActivityTimelineChartMarkerView(Context context) {
        super(context, R.layout.view_activity_timeline_chart_marker_view);

        mTitleView = (TextView) findViewById(android.R.id.title);
    }

    @Override
    public void refreshContent(Entry e, int dataSetIndex) {
        String text = TimerTextFormatter.formatTaskTimerText(
                mTitleView.getResources(), (int)e.getData());
        mTitleView.setText(Html.fromHtml(text));
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
