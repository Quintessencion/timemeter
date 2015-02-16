package com.simbirsoft.timemeter.ui.stats;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.MarkerView;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.util.TimerTextFormatter;

public class ChartMarkerView extends MarkerView {

    private TextView mTitleView;
    private TextView mSubtitleView;
    private String[] labels;

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param labels labels to display on a marker
     */
    public ChartMarkerView(Context context, String[] labels) {
        super(context, R.layout.marker_view);

        this.labels = labels;
        mTitleView = (TextView) findViewById(android.R.id.title);
        mSubtitleView = (TextView) findViewById(R.id.subtitle);
    }

    @Override
    public void refreshContent(Entry e, int dataSetIndex) {
        String timeText = TimerTextFormatter.formatTaskTimerText(
                mTitleView.getResources(), (int) e.getVal());
        mTitleView.setText(labels[e.getXIndex()]);
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
