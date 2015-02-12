package com.simbirsoft.timemeter.ui.main;

import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.simbirsoft.timemeter.ui.views.FilterView;

public interface ContentFragmentCallbacks {
    public RelativeLayout getContainerUnderlayView();
    public RelativeLayout getContentRootView();
    public FrameLayout getContainerView();
    public FrameLayout getContainerHeaderView();
}
