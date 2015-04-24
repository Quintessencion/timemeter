package com.simbirsoft.timemeter.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.model.TaskActivityDateItem;
import com.simbirsoft.timemeter.ui.model.TaskActivityItem;
import com.simbirsoft.timemeter.ui.model.TaskActivityListItem;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

@EFragment(R.layout.fragment_task_activities)
public class TaskActivitiesFragment extends BaseFragment {
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TITLE = "extra_title";

    @ViewById(android.R.id.list)
    RecyclerView mRecyclerView;


    @FragmentArg(EXTRA_TASK_ID)
    Long mExtraTaskId;

    @FragmentArg(EXTRA_TITLE)
    String mExtraTitle;

    private ActionBar mActionBar;
    private TaskActivitiesAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setShouldSubscribeForJobEvents(false);
        super.onCreate(savedInstanceState);
        Injection.sUiComponent.injectTaskActivitiesFragment(this);
    }

    @AfterViews
    void bindViews() {
        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        if (mExtraTitle != null) {
           mActionBar.setTitle(mExtraTitle);
        }
        ArrayList<TaskActivityItem> items = Lists.newArrayList();
        Calendar calendar = Calendar.getInstance();
        TaskActivityDateItem item1 = new TaskActivityDateItem();
        calendar.set(2015, 4, 1);
        item1.setDate(calendar.getTime());
        items.add(item1);

        TaskActivityListItem item2 = new TaskActivityListItem();
        calendar.set(2015, 4, 3);
        item2.setDate(calendar.getTime());
        items.add(item2);

        TaskActivityListItem item3 = new TaskActivityListItem();
        calendar.set(2015, 4, 20);
        item3.setDate(calendar.getTime());
        items.add(item3);

        TaskActivityDateItem item4 = new TaskActivityDateItem();
        calendar.set(2015, 5, 1);
        item1.setDate(calendar.getTime());
        items.add(item4);

        TaskActivityListItem item5 = new TaskActivityListItem();
        calendar.set(2015, 4, 20);
        item5.setDate(calendar.getTime());
        items.add(item5);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new TaskActivitiesAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setItems(items);
    }

}
