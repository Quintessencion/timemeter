package com.simbirsoft.timemeter.ui.stats.binders;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
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
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.model.DailyActivityDuration;
import com.simbirsoft.timemeter.ui.model.DailyTaskActivityDuration;
import com.simbirsoft.timemeter.ui.stats.ActivityTimelineChartMarkerView;
import com.simbirsoft.timemeter.ui.stats.StatisticsViewBinder;
import com.simbirsoft.timemeter.ui.util.TimerTextFormatter;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class ActivityTimelineBinder implements StatisticsViewBinder, OnChartValueSelectedListener {

    private static final Logger LOG = LogFactory.getLogger(ActivityTimelineBinder.class);

    @Inject
    Context mContext;

    private ViewGroup mContentRoot;
    private LineChart mChart;
    private TextView mTitleView;
    private TextView mSummaryActivityView;
    private final List<DailyActivityDuration> mActivityTimeline;
    private int mLineColor;
    private boolean mIsDataBound;
    private TextView mEmptyIndicatorView;
    private boolean mIsFullScreenMode;

    public ActivityTimelineBinder(List<DailyActivityDuration> activityTimeline) {
        mActivityTimeline = activityTimeline;

        Injection.sUiComponent.injectActivityTimelineBinder(this);
    }

    @Override
    public int getViewTypeId() {
        return VIEW_TYPE_ACTIVITY_TIMELINE;
    }

    @Override
    public View createView(Context context, ViewGroup parent, boolean fullScreenMode) {
        mContentRoot = (ViewGroup) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_activity_timeline, parent, false);
        mIsFullScreenMode = fullScreenMode;
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

    @Override
    public String getTitle() {
        return mContext.getString(R.string.title_activity_timeline);
    }

    private void initializeChart() {
        mLineColor = mContext.getResources().getColor(R.color.primary);

        mSummaryActivityView = (TextView) mContentRoot.findViewById(R.id.summaryActivityView);
        mSummaryActivityView.setText(getFormattedTotalTime());

        mChart = (LineChart) mContentRoot.findViewById(R.id.chart);
        mEmptyIndicatorView = (TextView) mContentRoot.findViewById(android.R.id.empty);
        mEmptyIndicatorView.setVisibility(View.GONE);
        mTitleView = (TextView) mContentRoot.findViewById(android.R.id.title);
        mTitleView.setText(getTitle());
        if (mIsFullScreenMode) {
            mTitleView.setVisibility(View.GONE);
        }
        mChart.setDescription("");
        mChart.setDrawXLabels(true);
        mChart.setDrawYLabels(true);
        mChart.setDrawYValues(false);
        mChart.setDrawLegend(false);
        mChart.setTouchEnabled(mIsFullScreenMode);
        mChart.setClickable(mIsFullScreenMode);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setOffsets(0f, 0f, 0f, 0f);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawMarkerViews(true);
        mChart.setValueTextColor(mContentRoot.getResources().getColor(R.color.accentPrimary));
        mChart.setMarkerView(new ActivityTimelineChartMarkerView(mContentRoot.getContext()));

        measureChartView(mContext.getResources());
    }

    private void measureChartView(Resources res) {
        int size = res.getDisplayMetrics().widthPixels;
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.AT_MOST);

        if (mActivityTimeline.isEmpty()) {
            mChart.setMinimumHeight(0);
            mChart.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST));
            return;
        }

        int preferredHeight = (mIsFullScreenMode) ? (int) res.getDimension(R.dimen.chart_full_screen_height) :
                                                    (int) res.getDimension(R.dimen.chart_height);
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

    private String getFormattedTotalTime() {
        final String formattedTime = TimerTextFormatter.formatTaskSpanText(mContext.getResources(),
                calculateTotalTime());
        return String.format(mContext.getString(R.string.chart_legend_totally),
                Html.fromHtml(formattedTime).toString());
    }

    private long calculateTotalTime () {
        long totalTime = 0;

        for (DailyActivityDuration activityDuration: mActivityTimeline) {
            totalTime += activityDuration.duration;
        }
        return totalTime;
    }
}
