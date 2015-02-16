package com.simbirsoft.timemeter.ui.stats.binders;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Legend;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.model.TaskOverallActivity;
import com.simbirsoft.timemeter.ui.stats.ChartMarkerView;
import com.simbirsoft.timemeter.ui.stats.StatisticsViewBinder;
import com.simbirsoft.timemeter.ui.views.VerticalChartLegendView;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class OverallActivityTimePieBinder implements StatisticsViewBinder {

    private static final Logger LOG = LogFactory.getLogger(OverallActivityTimePieBinder.class);

    final String[] titles = new String[] {
            "task 1", "task 2", "task3", "task 4", "task 5"
    };

    private ViewGroup mContentRoot;
    private PieChart mPieChart;
    private TextView mTitleView;
    private final List<TaskOverallActivity> mOverallActivity;
    private Legend mLegend;
    private VerticalChartLegendView mVerticalChartLegendView;

    public OverallActivityTimePieBinder(List<TaskOverallActivity> overallActivity) {
        mOverallActivity = overallActivity;
    }

    @Override
    public int getViewTypeId() {
        return VIEW_TYPE_ACTIVITY_OVERALL_TIME_PIE;
    }

    @Override
    public View createView(Context context, ViewGroup parent) {
        mContentRoot = (ViewGroup) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_pie_chart, parent, false);
        mVerticalChartLegendView = (VerticalChartLegendView) mContentRoot.findViewById(R.id.legendPanel);

        mPieChart = (PieChart) mContentRoot.findViewById(R.id.chart);
        mTitleView = (TextView) mContentRoot.findViewById(android.R.id.title);
        mTitleView.setText(context.getString(R.string.title_overall_activity_pie_chart));
        mPieChart.setDescription("");
        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColorTransparent(true);
        mPieChart.setHoleRadius(30f);
        mPieChart.setTransparentCircleRadius(55f);
        mPieChart.setDrawCenterText(false);
        mPieChart.setRotationEnabled(false);
        mPieChart.setDrawXValues(false);
        mPieChart.setDrawYValues(true);
        mPieChart.setUsePercentValues(true);
        mPieChart.setDrawLegend(false);
        mPieChart.setTouchEnabled(true);
        mPieChart.setOffsets(0f, 0f, 0f, 0f);

//        mPieChart.setOnChartValueSelectedListener(this);

//        mPieChart.setDrawMarkerViews(true);
//        mPieChart.setMarkerView(new ChartMarkerView(context, titles));

        mPieChart.animateX(1500);
        measureChartView(context.getResources());

        return mContentRoot;
    }

    @Override
    public void bindView(View view) {
        final int count = mOverallActivity.size();
        final ArrayList<Entry> overallSpentTimeY = Lists.newArrayListWithCapacity(count);
        final ArrayList<String> titlesX = Lists.newArrayListWithCapacity(count);
        final int[] colors = new int[count];

        for (int i = 0; i < count; i++) {
            TaskOverallActivity item = mOverallActivity.get(i);
            overallSpentTimeY.add(new Entry((float) item.getDuration(), i));

            titlesX.add(item.getDescription());

            colors[i] = ColorTemplate.JOYFUL_COLORS[i % ColorTemplate.JOYFUL_COLORS.length];
        }

        PieDataSet pieDataSet = new PieDataSet(overallSpentTimeY, "");
        pieDataSet.setSliceSpace(1f);
        pieDataSet.setColors(colors);

        PieData data = new PieData(titlesX, pieDataSet);
        mPieChart.setData(data);

        mLegend = mPieChart.getLegend();
        mLegend.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        mLegend.setXEntrySpace(7f);
        mLegend.setYEntrySpace(0f);
        mLegend.setForm(Legend.LegendForm.CIRCLE);
        mLegend.setTextSize(16f);
        mLegend.setStackSpace(12f);

        mVerticalChartLegendView.setLegend(mLegend);

        // undo all highlights
        mPieChart.highlightValues(null);

        mPieChart.invalidate();
    }

    private void measureChartView(Resources res) {
        int size = res.getDisplayMetrics().widthPixels;
        int preferredHeight = (int) res.getDimension(R.dimen.chart_height);
        int minHeight = Math.min(preferredHeight, size);

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(minHeight, View.MeasureSpec.AT_MOST);

        mPieChart.setMinimumHeight(minHeight);
        mPieChart.measure(widthMeasureSpec, heightMeasureSpec);
    }
}
