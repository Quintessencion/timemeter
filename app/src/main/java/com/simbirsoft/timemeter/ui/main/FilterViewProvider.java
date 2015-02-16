package com.simbirsoft.timemeter.ui.main;

import com.simbirsoft.timemeter.ui.views.FilterView;

public interface FilterViewProvider {
    public FilterView getFilterView();
    public void hideFilterView();
}