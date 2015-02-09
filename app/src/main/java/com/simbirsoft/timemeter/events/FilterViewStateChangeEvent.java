package com.simbirsoft.timemeter.events;

import com.simbirsoft.timemeter.ui.views.FilterView;

public class FilterViewStateChangeEvent {
    private FilterView.FilterState mFilterState;

    public FilterViewStateChangeEvent(FilterView.FilterState filterState) {
        mFilterState = filterState;
    }

    public FilterView.FilterState getFilterState() {
        return mFilterState;
    }
}
