package com.simbirsoft.timemeter.ui.activities;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class TaskActivitiesLayoutManager extends LinearLayoutManager {
    private RecyclerView mRecyclerView;
    private int mItemIndex = -1;
    private int mSpanIndex = -1;

    public TaskActivitiesLayoutManager(Context context) {
        super(context);
    }

    public TaskActivitiesLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void onAttachedToWindow (RecyclerView view) {
        super.onAttachedToWindow(view);
        mRecyclerView = view;
    }

    @Override
    public void onDetachedFromWindow (RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        mRecyclerView = null;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (mItemIndex >= 0) {
            View view = recycler.getViewForPosition(mItemIndex);
            recycler.bindViewToPosition(view, mItemIndex);
            RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
            int widthSpec = View.MeasureSpec.makeMeasureSpec(mRecyclerView.getWidth(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                    getPaddingLeft() + getPaddingRight(), p.width);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                    getPaddingTop() + getPaddingBottom(), p.height);
            view.measure(childWidthSpec, childHeightSpec);
            int itemHeight = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;
            if (itemHeight > mRecyclerView.getHeight()) {
                TaskActivitiesAdapter adapter = (TaskActivitiesAdapter)mRecyclerView.getAdapter();
                int spansCount = adapter.getSpansCount(mItemIndex);
                int spanHeight = (view.getMeasuredHeight() - view.getPaddingTop() - view.getPaddingBottom()) / spansCount;
                int offset = view.getPaddingTop() + mSpanIndex * spanHeight;
                if (offset >= mRecyclerView.getHeight() - spanHeight) {
                    scrollToPositionWithOffset(mItemIndex, -offset);
                }
            }
            recycler.recycleView(view);
        }
        mItemIndex = -1;
        mSpanIndex = -1;
        super.onLayoutChildren(recycler, state);
    }

    public void scrollToSpan(int itemIndex, int spanIndex) {
        mItemIndex = itemIndex;
        mSpanIndex = spanIndex;
        scrollToPosition(itemIndex);
    }
}
