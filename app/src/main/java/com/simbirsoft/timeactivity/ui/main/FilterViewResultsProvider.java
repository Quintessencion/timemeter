package com.simbirsoft.timeactivity.ui.main;

import com.simbirsoft.timeactivity.ui.views.FilterView;

public interface FilterViewResultsProvider extends FilterViewProvider{
    void updateFilterResultsView(int taskCount, FilterView.FilterState filterState);
}
