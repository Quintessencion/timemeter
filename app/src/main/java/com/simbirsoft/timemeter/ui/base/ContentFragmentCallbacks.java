package com.simbirsoft.timemeter.ui.base;

import android.widget.RelativeLayout;

import com.simbirsoft.timemeter.ui.views.FilterView;

public interface ContentFragmentCallbacks {
    public RelativeLayout getFragmentContainerRoot();
    public FilterView getFilterView();
    public void hideFilterView();
}
