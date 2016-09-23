package com.simbirsoft.timeactivity.ui.main;

import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public interface ContentFragmentCallbacks {
    public RelativeLayout getContainerUnderlayView();
    public RelativeLayout getContentRootView();
    public FrameLayout getContainerView();
    public FrameLayout getContainerHeaderView();
}
