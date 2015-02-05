package com.simbirsoft.timemeter.ui.stats;

import android.os.Bundle;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.interfaces.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.MarkerView;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTagListJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_stats)
public class StatsFragment extends BaseFragment implements JobLoader.JobLoaderCallbacks,
        OnChartValueSelectedListener{


    private static final Logger LOG = LogFactory.getLogger(StatsFragment.class);

    final String[] titles = new String[] {
            "task 1", "task 2", "task3", "task 4", "task 5"
    };

    @ViewById(R.id.chart)
    PieChart chartView;


    @AfterViews
    void bindViews() {
        chartView.setDescription("Description");
        chartView.setDrawHoleEnabled(true);
        chartView.setHoleColorTransparent(true);
        chartView.setTransparentCircleRadius(55f);
        chartView.setHoleRadius(45f);
        chartView.setDrawCenterText(true);
        chartView.setCenterText("Center\nText");

        chartView.setDrawXValues(false);
        chartView.setDrawYValues(true);

        chartView.setOnChartValueSelectedListener(this);
        chartView.setTouchEnabled(true);

        chartView.setDrawMarkerViews(true);
        chartView.setMarkerView(new ChartMarkerView(getActivity(), titles));

        setData();
        //set data before adjusting legend!

        Legend l = chartView.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);

        chartView.setCenterTextSize(20f);
        chartView.setDescriptionTextSize(20f);
        l.setTextSize(16f);

        chartView.animateX(1500);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public Job onCreateJob(String tag) {
        return Injection.sJobsComponent.loadTagListJob();
    }

    @OnJobSuccess(LoadTagListJob.class)
    public void onTagListLoaded(LoadJobResult<List<Tag>> result) {

    }

    @OnJobFailure(LoadTagListJob.class)
    public void onTagListLoadFailed() {

    }


    @Override
    public void onValueSelected(Entry e, int dataSetIndex) {

    }

    @Override
    public void onNothingSelected() {

    }

    private void setData() {

        final int COUNT = titles.length;


        ArrayList<Entry> yVals1 = new ArrayList<>();

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        for (int i = 0; i < COUNT; i++) {
            yVals1.add(new Entry((float) (Math.random() * 100) + 10, i));
        }

        ArrayList<String> xVals = new ArrayList<>();

        for (int i = 0; i < COUNT; i++)
            xVals.add(titles[i]);

        PieDataSet set1 = new PieDataSet(yVals1, "");
        set1.setSliceSpace(0f);

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        set1.setColors(colors);

        PieData data = new PieData(xVals, set1);
        chartView.setData(data);

        // undo all highlights
        chartView.highlightValues(null);

        chartView.invalidate();
    }
}
