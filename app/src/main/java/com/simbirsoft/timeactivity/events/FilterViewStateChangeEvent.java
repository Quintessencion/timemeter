package com.simbirsoft.timeactivity.events;

import com.simbirsoft.timeactivity.ui.views.FilterView;

public class FilterViewStateChangeEvent {
    private FilterView.FilterState mFilterState;
    private boolean mIsReset;

    public FilterViewStateChangeEvent(FilterView.FilterState filterState) {
        mFilterState = filterState;
    }

    public FilterView.FilterState getFilterState() {
        return mFilterState;
    }

    public boolean isReset() {
        return mIsReset;
    }

    public void setReset(boolean isReset) {
        mIsReset = isReset;
    }
}
