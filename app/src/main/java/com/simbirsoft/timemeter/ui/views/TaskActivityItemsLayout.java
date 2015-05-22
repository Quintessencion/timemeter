package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.ui.model.TaskActivitySpansItem;

import java.util.ArrayList;
import java.util.List;

public class TaskActivityItemsLayout extends LinearLayout implements TaskActivitySpansItem.OnTaskActivityChangedListener {
    public interface TaskActivityItemsAdapter {
        public View getActivityItemView(TaskActivityItemsLayout layout);
        public void addActivityItemViews(List<View> items);
    }

    private TaskActivitySpansItem mItem;

    private TaskActivityItemsAdapter mAdapter;

    public TaskActivityItemsLayout(Context context) {
        super(context);
    }

    public TaskActivityItemsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TaskActivityItemsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TaskActivityItemsLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mItem = new TaskActivitySpansItem();
    }

    public void setTaskActivitySpansItem(TaskActivitySpansItem item) {
        mItem.setOnChangedListener(null);
        mItem = item;
        mItem.setOnChangedListener(this);
        update();
    }

    public void setAdapter(TaskActivityItemsAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public void onTaskActivityChanged(int index) {
        TaskActivityItemView itemView = (TaskActivityItemView)getChildAt(0).getTag();
        itemView.invalidate();
    }

    private void update() {
        if (mAdapter == null) {
            throw new IllegalStateException("TaskActivityItemsAdapter is not set");
        }
        int spansCount = mItem.getSpansCount();
        int childCount = getChildCount();
        if (childCount > spansCount) {
            removeItems(childCount - spansCount);
        }
        for (int i = 0; i < spansCount; i++) {
            View itemView;
            if (i < childCount) {
                itemView = getChildAt(i);
            } else {
                itemView = mAdapter.getActivityItemView(this);
                addView(itemView, i);
            }
            ((TaskActivityItemView)itemView.getTag()).setTaskActivitySpansItem(mItem, i);
        }
    }

    private void removeItems(int count) {
        int childCount = getChildCount();
        ArrayList<View> views = Lists.newArrayList();
        for (int i = childCount - count; i < childCount; i++) {
            views.add(getChildAt(i));
        }
        mAdapter.addActivityItemViews(views);
        removeViews(childCount - count, count);
    }

}
