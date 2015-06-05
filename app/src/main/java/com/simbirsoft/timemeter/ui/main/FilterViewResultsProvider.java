package com.simbirsoft.timemeter.ui.main;

import com.simbirsoft.timemeter.ui.views.FilterView;

public interface FilterViewResultsProvider extends FilterViewProvider{
    void updateFilterResultsView(int taskCount, FilterView.FilterState filterState);
}
