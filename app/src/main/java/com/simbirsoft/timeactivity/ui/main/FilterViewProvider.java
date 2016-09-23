package com.simbirsoft.timeactivity.ui.main;

import com.simbirsoft.timeactivity.ui.views.FilterView;

public interface FilterViewProvider {
    public FilterView getFilterView();
    public void hideFilterView();
}
