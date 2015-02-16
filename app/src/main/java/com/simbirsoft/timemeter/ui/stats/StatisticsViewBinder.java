package com.simbirsoft.timemeter.ui.stats;

import android.content.Context;
import android.support.annotation.IntDef;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface StatisticsViewBinder {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VIEW_TYPE_ACTIVITY_OVERALL_TIME_PIE})
    public @interface BinderViewType {
    }

    public static final int VIEW_TYPE_ACTIVITY_OVERALL_TIME_PIE = 0;

    @BinderViewType
    public int getViewTypeId();
    public View createView(Context context, ViewGroup parent);
    public void bindView(View view);
}
