package com.simbirsoft.timemeter.ui.stats.binders;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.interfaces.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.QueryUtils;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.model.TaskOverallActivity;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.stats.OverallTaskActivityChartMarkerView;
import com.simbirsoft.timemeter.ui.stats.StatisticsViewBinder;
import com.simbirsoft.timemeter.ui.util.ColorSets;
import com.simbirsoft.timemeter.ui.util.TimerTextFormatter;
import com.simbirsoft.timemeter.ui.views.VerticalLegend;

import org.slf4j.Logger;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class OverallActivityTimePieBinder implements StatisticsViewBinder,
        OnChartValueSelectedListener, VerticalLegend.LegendClickListener {

    private static final Logger LOG = LogFactory.getLogger(OverallActivityTimePieBinder.class);

    @Inject
    Context mContext;

    @Inject
    Resources mResources;

    @Inject
    SQLiteDatabase mSQLiteDatabase;

    private ViewGroup mContentRoot;
    private PieChart mPieChart;
    private TextView mTitleView;
    private final List<TaskOverallActivity> mOverallActivity;
    private Legend mLegend;
    private VerticalLegend mVerticalLegend;
    private TextView mEmptyIndicatorView;
    private boolean mIsDataBound;
    private boolean mIsFullScreenMode;
    private Paint mCenterTextPaint;

    private StatisticsViewBinder.OnLegendClickListener mOnLegendClickListener;

    public OverallActivityTimePieBinder(List<TaskOverallActivity> overallActivity) {
        mOverallActivity = overallActivity;

        Injection.sUiComponent.injectOverallActivityTimePieBinder(this);
    }

    @Override
    public int getViewTypeId() {
        return VIEW_TYPE_ACTIVITY_OVERALL_TIME_PIE;
    }

    @Override
    public View createView(Context context, ViewGroup parent, boolean fullScreenMode) {
        mContentRoot = (ViewGroup) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_pie_chart, parent, false);
        mIsFullScreenMode = fullScreenMode;
        initializePieChart();
        return mContentRoot;
    }

    @Override
    public void bindView(View view) {
        if (mContentRoot == null) {
            mContentRoot = (ViewGroup) view;
            initializePieChart();
        }

        if (mIsDataBound) {
            return;
        }

        final int count = mOverallActivity.size();
        final ArrayList<Entry> overallSpentTimeY = Lists.newArrayListWithCapacity(count);
        final ArrayList<String> titlesX = Lists.newArrayListWithCapacity(count);
        final int[] colors = new int[count];
        long overallDuration = 0;

        for (int i = 0; i < count; i++) {
            TaskOverallActivity item = mOverallActivity.get(i);
            overallSpentTimeY.add(new Entry(
                    (float) item.getDuration(),
                    i,
                    item));
            overallDuration += item.getDuration();

            titlesX.add(item.getDescription());

            colors[i] = ColorSets.getTaskColor(item.getId());
        }

        PieDataSet pieDataSet = new PieDataSet(overallSpentTimeY, "");
        pieDataSet.setSliceSpace(1f);
        pieDataSet.setColors(colors);

        PieData data = new PieData(titlesX, pieDataSet);
        mPieChart.setData(data);

        mPieChart.setMarkerView(new OverallTaskActivityChartMarkerView(mContentRoot.getContext()));
        mPieChart.setCenterText(TimerTextFormatter.formatOverallTimePlain(mResources, overallDuration));

        if (mLegend == null) {
            mLegend = mPieChart.getLegend();
            mLegend.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
            mLegend.setXEntrySpace(7f);
            mLegend.setYEntrySpace(7f);
            mLegend.setForm(Legend.LegendForm.CIRCLE);
            mLegend.setTextSize(16f);
            mLegend.setStackSpace(12f);
            mVerticalLegend.setLegend(mLegend);
            mPieChart.highlightValues(null);
        }

        measureChartView(mContentRoot.getResources());
        mPieChart.invalidate();
        mVerticalLegend.update();

        if (mOverallActivity.isEmpty()) {
            mEmptyIndicatorView.setVisibility(View.VISIBLE);
        } else {
            mEmptyIndicatorView.setVisibility(View.GONE);
        }

        mIsDataBound = true;
    }

    @Override
    public String getTitle() {
        return mContext.getString(R.string.title_overall_activity_pie_chart);
    }

    private void initializePieChart() {
        final DecimalFormat format = new DecimalFormat("#.#");

        mVerticalLegend = (VerticalLegend) mContentRoot.findViewById(R.id.legendPanel);
        mVerticalLegend.setLegendClickListener(this);

        mPieChart = (PieChart) mContentRoot.findViewById(R.id.chart);
        mTitleView = (TextView) mContentRoot.findViewById(android.R.id.title);
        mTitleView.setText(getTitle());

        if (mIsFullScreenMode) {
            mTitleView.setVisibility(View.GONE);
        }

        mEmptyIndicatorView = (TextView) mContentRoot.findViewById(android.R.id.empty);
        mEmptyIndicatorView.setVisibility(View.GONE);
        mPieChart.setDescription("");
        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColorTransparent(true);
        mPieChart.setHoleRadius(35f);
        mPieChart.setTransparentCircleRadius(45f);
        mPieChart.setDrawCenterText(true);
        mPieChart.setRotationEnabled(false);
        mPieChart.setDrawXValues(false);
        mPieChart.setDrawYValues(true);
        mPieChart.setUsePercentValues(true);
        mPieChart.setDrawLegend(false);
        mPieChart.setTouchEnabled(mIsFullScreenMode);
        mPieChart.setClickable(mIsFullScreenMode);
        mPieChart.setOffsets(0f, 0f, 0f, 0f);
        mPieChart.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value < 3f) {
                    return "";
                }

                return format.format(value) + " %";
            }
        });

        mCenterTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterTextPaint.setColor(mResources.getColor(R.color.darkGrey));
        mCenterTextPaint.setTextSize(Utils.convertDpToPixel(12f));
        mCenterTextPaint.setTextAlign(Paint.Align.CENTER);
        mPieChart.setPaint(mCenterTextPaint, PieChart.PAINT_CENTER_TEXT);

        mPieChart.setOnChartValueSelectedListener(this);

        mPieChart.setDrawMarkerViews(true);

        measureChartView(mContext.getResources());
    }

    private void measureChartView(Resources res) {
        int size = res.getDisplayMetrics().widthPixels;
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.AT_MOST);

        if (mOverallActivity.isEmpty()) {
            mPieChart.setMinimumHeight(0);
            mPieChart.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST));
            return;
        }

        int preferredHeight = (mIsFullScreenMode) ? (int) res.getDimension(R.dimen.chart_full_screen_height) :
                                                    (int) res.getDimension(R.dimen.chart_height);
        int minHeight = Math.min(preferredHeight, size);

        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(minHeight, View.MeasureSpec.AT_MOST);

        mPieChart.setMinimumHeight(minHeight);
        mPieChart.measure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex) {
    }

    @Override
    public void onNothingSelected() {
    }

    @Override
    public void onLabelClicked(int position) {
        TaskOverallActivity taskOverallActivity = mOverallActivity.get(position);
        List<Tag> tags = QueryUtils.getTagsForTask(mSQLiteDatabase, taskOverallActivity.getId());

        if (mOnLegendClickListener != null) {
            mOnLegendClickListener.onLegendItemClicked(TaskBundle.create(taskOverallActivity, tags));
        }
    }

    public void setOnLegendClickListener(OnLegendClickListener onLegendClickListener) {
        mOnLegendClickListener = onLegendClickListener;
    }
}
