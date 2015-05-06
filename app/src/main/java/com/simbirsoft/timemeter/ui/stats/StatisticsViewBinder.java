package com.simbirsoft.timemeter.ui.stats;

import android.content.Context;
import android.support.annotation.IntDef;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface StatisticsViewBinder {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            VIEW_TYPE_ACTIVITY_OVERALL_TIME_PIE,
            VIEW_TYPE_ACTIVITY_TIMELINE,
            VIEW_TYPE_ACTIVITY_STACKED_TIMELINE
    })
    public @interface BinderViewType {
    }

    public static final int VIEW_TYPE_ACTIVITY_OVERALL_TIME_PIE = 0;
    public static final int VIEW_TYPE_ACTIVITY_TIMELINE = 1;
    public static final int VIEW_TYPE_ACTIVITY_STACKED_TIMELINE = 2;

    @BinderViewType
    public int getViewTypeId();
    public View createView(Context context, ViewGroup parent, boolean touchable);
    public void bindView(View view);
    public String getTitle();
}
