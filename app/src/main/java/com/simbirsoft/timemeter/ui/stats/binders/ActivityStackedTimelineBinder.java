package com.simbirsoft.timemeter.ui.stats.binders;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Legend;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.model.DailyTaskActivityDuration;
import com.simbirsoft.timemeter.ui.stats.ActivityStackedTimelineChartMarkerView;
import com.simbirsoft.timemeter.ui.stats.StatisticsViewBinder;
import com.simbirsoft.timemeter.ui.util.ColorSets;
import com.simbirsoft.timemeter.ui.views.VerticalChartLegendView;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ActivityStackedTimelineBinder implements StatisticsViewBinder, OnChartValueSelectedListener {

    private static final Logger LOG = LogFactory.getLogger(ActivityStackedTimelineBinder.class);

    private ViewGroup mContentRoot;
    private BarChart mChart;
    private TextView mTitleView;
    private final List<DailyTaskActivityDuration> mActivityTimeline;
    private VerticalChartLegendView mVerticalChartLegendView;
    private Legend mLegend;
    private boolean mIsDataBound;

    public ActivityStackedTimelineBinder(List<DailyTaskActivityDuration> activityTimeline) {
        mActivityTimeline = activityTimeline;
    }

    @Override
    public int getViewTypeId() {
        return VIEW_TYPE_ACTIVITY_STACKED_TIMELINE;
    }

    @Override
    public View createView(Context context, ViewGroup parent) {
        mContentRoot = (ViewGroup) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_activity_stacked_timeline, parent, false);

        initializeChart();

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
        final ArrayList<BarEntry> timelineY = Lists.newArrayListWithCapacity(count);
        final ArrayList<String> timelineX = Lists.newArrayListWithCapacity(count);
        String[] taskLabels = null;
        int stackCount = 0;
        if (count > 0) {
            stackCount = mActivityTimeline.get(0).tasks.length;
        }

        for (int i = 0; i < count; i++) {
            DailyTaskActivityDuration item = mActivityTimeline.get(i);

            float[] yVals = new float[item.tasks.length];
            for (int j = 0; j < yVals.length; j++) {
                int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(item.tasks[j].getDuration());
                yVals[j] = minutes / 60f;

                if (i == 0) {
                    if (taskLabels == null) {
                        taskLabels = new String[item.tasks.length];
                    }
                    taskLabels[j] = item.tasks[j].getDescription();
                }
            }

            BarEntry barEntry = new BarEntry(yVals, i);
            barEntry.setData(item);
            timelineY.add(barEntry);
            timelineX.add(DateFormat.format("dd.MM", item.date).toString());
        }

        BarDataSet dataSet = new BarDataSet(timelineY, "");
        dataSet.setColors(ColorSets.makeColorSet(ColorSets.MIXED_COLORS, stackCount));
        dataSet.setStackLabels(taskLabels);

        mChart.setData(new BarData(timelineX, dataSet));
        mChart.setMarkerView(new ActivityStackedTimelineChartMarkerView(mContentRoot.getContext(), mChart));
        mChart.setHighlightEnabled(true);
        mChart.setHighlightIndicatorEnabled(true);
        mChart.setDrawHighlightArrow(false);

        if (mLegend == null) {
            mLegend = mChart.getLegend();
            mLegend.setXEntrySpace(7f);
            mLegend.setYEntrySpace(0f);
            mLegend.setForm(Legend.LegendForm.CIRCLE);
            mLegend.setTextSize(16f);
            mLegend.setStackSpace(12f);
            mVerticalChartLegendView.setLegend(mLegend);
        }

        measureChartView(mContentRoot.getResources());
        mChart.invalidate();
        mVerticalChartLegendView.requestLayout();

        mIsDataBound = true;
    }

    private void initializeChart() {
        final Context context = mContentRoot.getContext();

        mVerticalChartLegendView = (VerticalChartLegendView) mContentRoot.findViewById(R.id.legendPanel);

        mChart = (BarChart) mContentRoot.findViewById(R.id.chart);
        mTitleView = (TextView) mContentRoot.findViewById(android.R.id.title);
        mTitleView.setText(context.getString(R.string.title_activity_stacked_timeline));
        mChart.setDescription("");
        mChart.setDrawXLabels(true);
        mChart.setDrawYLabels(true);
        mChart.setDrawYValues(false);
        mChart.setDrawLegend(false);
        mChart.setTouchEnabled(true);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setOffsets(0f, 0f, 0f, 0f);
        mChart.setValueTextColor(mContentRoot.getResources().getColor(R.color.accentPrimary));
        mChart.setOnChartValueSelectedListener(this);
//        mChart.setDrawMarkerViews(true);
//        mChart.setMarkerView(new ActivityTimelineChartMarkerView(mContentRoot.getContext()));

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