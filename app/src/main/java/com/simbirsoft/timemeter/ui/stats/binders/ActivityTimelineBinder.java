package com.simbirsoft.timemeter.ui.stats.binders;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.OnChartValueSelectedListener;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.model.DailyActivityDuration;
import com.simbirsoft.timemeter.ui.stats.ActivityTimelineChartMarkerView;
import com.simbirsoft.timemeter.ui.stats.StatisticsViewBinder;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ActivityTimelineBinder implements StatisticsViewBinder, OnChartValueSelectedListener {

    private static final Logger LOG = LogFactory.getLogger(ActivityTimelineBinder.class);

    private ViewGroup mContentRoot;
    private LineChart mChart;
    private TextView mTitleView;
    private final List<DailyActivityDuration> mActivityTimeline;
    private int mLineColor;
    private boolean mIsDataBound;
    private TextView mEmptyIndicatorView;

    public ActivityTimelineBinder(List<DailyActivityDuration> activityTimeline) {
        mActivityTimeline = activityTimeline;
    }

    @Override
    public int getViewTypeId() {
        return VIEW_TYPE_ACTIVITY_TIMELINE;
    }

    @Override
    public View createView(Context context, ViewGroup parent, boolean touchable) {
        mContentRoot = (ViewGroup) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_activity_timeline, parent, false);

        initializeChart();
        mChart.setTouchEnabled(touchable);
        mChart.setClickable(touchable);
        return mContentRoot;
    }

    @Override
    public void bindView(View view) {
        if (mContentRoot == null) {
            mContentRoot = (ViewGroup) view;
            initializeChart();
        }

        if (mIsDataBound) {
            return;
        }

        final int count = mActivityTimeline.size();
        final ArrayList<Entry> timelineY = Lists.newArrayListWithCapacity(count);
        final ArrayList<String> timelineX = Lists.newArrayListWithCapacity(count);

        for (int i = 0; i < count; i++) {
            DailyActivityDuration item = mActivityTimeline.get(i);

            int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(item.duration);
            float hours = minutes / 60f;

            timelineY.add(new Entry(
                    hours,
                    i,
                    item.duration));
            timelineX.add(DateFormat.format("dd.MM", item.date).toString());
        }

        LineDataSet dataSet = new LineDataSet(timelineY, "");
        dataSet.setCircleSize(3f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(mLineColor);
        dataSet.setLineWidth(2f);
        dataSet.setColor(mLineColor);

        mChart.setData(new LineData(timelineX, dataSet));

        measureChartView(mContentRoot.getResources());
        mChart.invalidate();

        if (mActivityTimeline.isEmpty()) {
            mEmptyIndicatorView.setVisibility(View.VISIBLE);
        } else {
            mEmptyIndicatorView.setVisibility(View.GONE);
        }

        mIsDataBound = true;
    }

    private void initializeChart() {
        final Context context = mContentRoot.getContext();

        mLineColor = context.getResources().getColor(R.color.primary);

        mChart = (LineChart) mContentRoot.findViewById(R.id.chart);
        mEmptyIndicatorView = (TextView) mContentRoot.findViewById(android.R.id.empty);
        mEmptyIndicatorView.setVisibility(View.GONE);
        mTitleView = (TextView) mContentRoot.findViewById(android.R.id.title);
        mTitleView.setText(context.getString(R.string.title_activity_timeline));
        mChart.setDescription("");
        mChart.setDrawXLabels(true);
        mChart.setDrawYLabels(true);
        mChart.setDrawYValues(false);
        mChart.setDrawLegend(false);
        mChart.setTouchEnabled(true);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setOffsets(0f, 0f, 0f, 0f);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawMarkerViews(true);
        mChart.setValueTextColor(mContentRoot.getResources().getColor(R.color.accentPrimary));
        mChart.setMarkerView(new ActivityTimelineChartMarkerView(mContentRoot.getContext()));

        measureChartView(context.getResources());
    }

    private void measureChartView(Resources res) {
        int size = res.getDisplayMetrics().widthPixels;
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.AT_MOST);

        if (mActivityTimeline.isEmpty()) {
            mChart.setMinimumHeight(0);
            mChart.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST));
            return;
        }

        int preferredHeight = (int) res.getDimension(R.dimen.chart_height);
        int minHeight = Math.min(preferredHeight, size);

        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(minHeight, View.MeasureSpec.AT_MOST);

        mChart.setMinimumHeight(minHeight);
        mChart.measure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex) {
    }

    @Override
    public void onNothingSelected() {
    }
}
