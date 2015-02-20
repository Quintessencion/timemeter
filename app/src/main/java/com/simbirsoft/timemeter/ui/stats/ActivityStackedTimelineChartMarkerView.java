package com.simbirsoft.timemeter.ui.stats;

import android.content.Context;
import android.graphics.Rect;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.MarkerView;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.ui.model.DailyTaskActivityDuration;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.util.TimerTextFormatter;

import java.util.concurrent.TimeUnit;

public class ActivityStackedTimelineChartMarkerView extends MarkerView {

    private TextView mTitleView;
    private TextView mTimeText;
    private BarChart mChart;

    public ActivityStackedTimelineChartMarkerView(Context context, BarChart chart) {
        super(context, R.layout.view_activity_stacked_timeline_chart_marker_view);

        mChart = chart;

        mTitleView = (TextView) findViewById(android.R.id.title);
        mTimeText = (TextView) findViewById(R.id.timeText);
    }

    @Override
    public void refreshContent(Entry e, int dataSetIndex) {
        BarEntry entry = (BarEntry) e;
        int index = mChart.getHighlightStackIndex(entry);
        float value;
        if (index > -1) {
            value = entry.getVals()[index];
        } else {
            value = entry.getVal();
        }

        int millis = (int) TimeUnit.MINUTES.toMillis((int) (value * 60));
        String text = TimerTextFormatter.formatTaskTimerText(mTitleView.getResources(), millis);
        mTimeText.setText(Html.fromHtml(text));

        if (index > -1) {
            DailyTaskActivityDuration item = (DailyTaskActivityDuration) entry.getData();
            Task task = item.tasks[index];
            mTitleView.setText(task.getDescription());
            mTitleView.setVisibility(View.VISIBLE);
        } else {
            mTitleView.setVisibility(View.GONE);
        }
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
