package com.simbirsoft.timemeter.ui.stats;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.ui.base.BaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_stats_details)
public class StatsDetailsFragment extends BaseFragment {

    private ActionBar mActionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setShouldSubscribeForJobEvents(false);
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void bindViews() {
        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_action_accept);
    }

}
