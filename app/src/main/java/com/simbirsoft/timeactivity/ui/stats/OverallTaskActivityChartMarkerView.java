package com.simbirsoft.timeactivity.ui.stats;

import android.content.Context;
import android.text.Html;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.MarkerView;
import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.model.TaskOverallActivity;
import com.simbirsoft.timeactivity.ui.util.TimerTextFormatter;

import java.text.DecimalFormat;

public class OverallTaskActivityChartMarkerView extends MarkerView {

    private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("#.#");

    private TextView mTitleView;
    private TextView mSubtitleView;

    public OverallTaskActivityChartMarkerView(Context context) {
        super(context, R.layout.view_overall_activity_chart_marker_view);

        mTitleView = (TextView) findViewById(android.R.id.title);
        mSubtitleView = (TextView) findViewById(R.id.subtitle);
    }

    @Override
    public void refreshContent(Entry e, int dataSetIndex) {
        TaskOverallActivity item = (TaskOverallActivity) e.getData();
        String timeText = TimerTextFormatter.formatTaskTimerText(
                mTitleView.getResources(), (int) e.getVal());

        timeText += " (" + PERCENTAGE_FORMAT.format(item.getDurationRatio() * 100) + "%)";

        mTitleView.setText(item.getDescription());
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
