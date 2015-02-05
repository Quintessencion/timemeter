package com.simbirsoft.timemeter.ui.stats;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.MarkerView;
import com.simbirsoft.timemeter.R;

public class ChartMarkerView extends MarkerView {

    private TextView contentView;
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
        contentView = (TextView) findViewById(R.id.content);
    }

    @Override
    public void refreshContent(Entry e, int dataSetIndex) {
        contentView.setText(String.format("%s\n%d", labels[e.getXIndex()], (int)e.getVal()));
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
