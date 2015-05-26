package com.simbirsoft.timemeter.ui.activities;


import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Preconditions;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.ui.model.TaskActivityDateItem;
import com.simbirsoft.timemeter.ui.model.TaskActivityEmptyItem;
import com.simbirsoft.timemeter.ui.model.TaskActivityItem;
import com.simbirsoft.timemeter.ui.model.TaskActivitySpansItem;
import com.simbirsoft.timemeter.ui.util.TimeUtils;
import com.simbirsoft.timemeter.ui.views.TaskActivityItemView;
import com.simbirsoft.timemeter.ui.views.TaskActivityItemsLayout;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

public class TaskActivitiesAdapter extends  RecyclerView.Adapter<TaskActivitiesAdapter.ViewHolder>
                                   implements TaskActivityItemsLayout.TaskActivityItemsAdapter,
                                    View.OnLongClickListener {

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class DateItemViewHolder extends ViewHolder {
        TextView mTextView;

        public DateItemViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView)itemView.findViewById(R.id.textView);
        }
    }

    static class SpansItemViewHolder extends EmptyItemViewHolder {
        TaskActivityItemsLayout mActivitiesLayout;

        public SpansItemViewHolder(View itemView, TaskActivityItemsLayout.TaskActivityItemsAdapter adapter) {
            super(itemView);
            mActivitiesLayout = (TaskActivityItemsLayout)itemView.findViewById(R.id.taskActivityItemsLayout);
            mActivitiesLayout.setAdapter(adapter);
        }
    }

    static class EmptyItemViewHolder extends ViewHolder {
        TextView mWeekDayTextView;
        TextView mDateTextView;

        public EmptyItemViewHolder(View itemView) {
            super(itemView);
            mWeekDayTextView = (TextView)itemView.findViewById(R.id.weekDayTextView);
            mDateTextView = (TextView)itemView.findViewById(R.id.dateTextView);
        }
    }

    private final List<TaskActivityItem> mItems;
    private final HashSet<View> mActivityItemViews;
    private Context mContext;
    private int mMiddleItemPaddingTop;
    private int mMiddleItemPaddingBottom;
    private int mFirstItemPaddingTop;
    private int mLastItemPaddingBottom;
    private int mDateColor;
    private int mCurrentDateColor;
    private int mHolidayDateColor;
    private final Calendar mCalendar;
    private final List<TaskTimeSpan> mHighlightedSpans;

    public TaskActivitiesAdapter(Context context) {
        mContext = context;
        mItems = Lists.newArrayList();
        mActivityItemViews = Sets.newHashSet();
        final Resources res = context.getResources();
        mMiddleItemPaddingTop = 0;
        mMiddleItemPaddingBottom = res.getDimensionPixelSize(R.dimen.task_activity_middle_item_padding_bottom);
        mFirstItemPaddingTop = res.getDimensionPixelSize(R.dimen.task_activity_first_item_padding_top);
        mLastItemPaddingBottom = res.getDimensionPixelSize(R.dimen.task_activity_last_item_padding_bottom);
        mDateColor = res.getColor(R.color.calendar_date_text);
        mCurrentDateColor = res.getColor(R.color.primary);
        mHolidayDateColor = res.getColor(R.color.accentPrimary);
        mCalendar = Calendar.getInstance();
        mHighlightedSpans = Lists.newArrayList();
    }

    public void setItems(List<TaskActivityItem> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public void setHighlightedSpans(List<TaskTimeSpan> spans) {
        mHighlightedSpans.clear();
        mHighlightedSpans.addAll(spans);
    }

    public int getItemViewType (int position) {
        TaskActivityItem item = mItems.get(position);
        return item.getItemType();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case TaskActivityItem.DATE_ITEM_TYPE:
                return createDateItemViewHolder(viewGroup);

            case TaskActivityItem.SPANS_ITEM_TYPE:
                return createSpansItemViewHolder(viewGroup);

            case TaskActivityItem.EMPTY_ITEM_TYPE:
                return createEmptyItemViewHolder(viewGroup);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        switch (getItemViewType(position)) {
            case TaskActivityItem.DATE_ITEM_TYPE:
                bindDateItemViewHolder((DateItemViewHolder)viewHolder, position);
                break;

            case TaskActivityItem.SPANS_ITEM_TYPE:
                bindSpansItemViewHolder((SpansItemViewHolder)viewHolder, position);
                break;

            case TaskActivityItem.EMPTY_ITEM_TYPE:
                bindEmptyItemViewHolder((EmptyItemViewHolder)viewHolder, position);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    private ViewHolder createDateItemViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.view_task_activity_date_item, viewGroup, false);

        return new DateItemViewHolder(view);
    }

    private ViewHolder createSpansItemViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.view_task_activity_spans_item, viewGroup, false);

        return new SpansItemViewHolder(view, this);
    }

    private ViewHolder createEmptyItemViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.view_task_activity_empty_item, viewGroup, false);

        return new EmptyItemViewHolder(view);
    }

    private void bindDateItemViewHolder(DateItemViewHolder viewHolder, int position) {
        TaskActivityDateItem item = (TaskActivityDateItem)mItems.get(position);
        viewHolder.mTextView.setText(item.getDateString());
    }

    private void bindSpansItemViewHolder(SpansItemViewHolder viewHolder, int position) {
        bindEmptyItemViewHolder(viewHolder, position);
        TaskActivitySpansItem item = (TaskActivitySpansItem)mItems.get(position);
        viewHolder.mActivitiesLayout.setTaskActivitySpansItem(item);
    }

    private void bindEmptyItemViewHolder(EmptyItemViewHolder viewHolder, int position) {
        TaskActivityEmptyItem item = (TaskActivityEmptyItem)mItems.get(position);
        int dateColor = mDateColor;
        long millis = item.getDateMillis();
        if (TimeUtils.isCurrentDay(millis, mCalendar)) {
            dateColor = mCurrentDateColor;
        } else if (TimeUtils.isHoliday(millis, mCalendar)) {
            dateColor = mHolidayDateColor;
        }
        viewHolder.mDateTextView.setTextColor(dateColor);
        viewHolder.mWeekDayTextView.setTextColor(dateColor);
        viewHolder.mDateTextView.setText(item.getDateString());
        viewHolder.mWeekDayTextView.setText(item.getWeekDayString());
        int topPadding = (isFirstItem(position)) ? mFirstItemPaddingTop : mMiddleItemPaddingTop;
        int bottomPadding = (isLastItem(position)) ? mLastItemPaddingBottom : mMiddleItemPaddingBottom;
        viewHolder.itemView.setPadding(0, topPadding, 0, bottomPadding);
    }

    private boolean isFirstItem(int position) {
        if (position == 0) return true;
        return getItemViewType(position - 1) == TaskActivityItem.DATE_ITEM_TYPE;
    }

    private boolean isLastItem(int position) {
        if (position == mItems.size() - 1) return true;
        return getItemViewType(position + 1) == TaskActivityItem.DATE_ITEM_TYPE;
    }

    @Override
    public View getActivityItemView(TaskActivityItemsLayout layout) {
        View view;
        if (mActivityItemViews.size() > 0) {
            view = mActivityItemViews.iterator().next();
            mActivityItemViews.remove(view);
        } else {
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.view_task_activity_item, layout, false);
            view.setTag(view.findViewById(R.id.taskActivityItemView));
            view.setOnLongClickListener(this);
        }
        return view;
    }

    @Override
    public void addActivityItemViews(List<View> items) {
        mActivityItemViews.addAll(items);
    }

    @Override
    public boolean isActivityItemViewHighlighted(TaskActivitySpansItem item, int index) {
        return mHighlightedSpans.contains(item.getSpan(index));
    }

    public void updateCurrentActivityTime(long taskId) {
        for (TaskActivityItem activityItem : mItems) {
            if (activityItem.getItemType() != TaskActivityItem.SPANS_ITEM_TYPE) {
                continue;
            }

            final TaskActivitySpansItem spansItem = (TaskActivitySpansItem) activityItem;

            int pos = Iterables.indexOf(spansItem.getList(), (item) -> item.getTaskId() == taskId);

            if (pos > -1) {
                spansItem.updateSpanEndTime(pos, System.currentTimeMillis());
                break;
            }
        }
    }

    public boolean getEarliestHighlightedSpanPosition(int[] position) {
        if (mHighlightedSpans.isEmpty()) {
            return false;
        }
        TaskTimeSpan span = mHighlightedSpans.get(0);
        for(int i = mItems.size() - 1; i >=0; i--) {
            TaskActivityItem item = mItems.get(i);
            if (item.getItemType() != TaskActivityItem.SPANS_ITEM_TYPE) continue;
            int spanIndex = ((TaskActivitySpansItem)item).indexOfSpan(span);
            if (spanIndex >=0) {
                position[0] = i;
                position[1] = spanIndex;
                return true;
            }
        }
        return false;
    }

    public int getSpansCount(int position) {
        Preconditions.checkElementIndex(position, mItems.size());
        TaskActivityItem item = mItems.get(position);
        Preconditions.checkArgument(item.getItemType() == TaskActivityItem.SPANS_ITEM_TYPE, "illegal item type");
        return ((TaskActivitySpansItem)item).getSpansCount();
    }

    @Override
    public boolean onLongClick(View v) {
        TaskActivityItemView itemView = (TaskActivityItemView)v.getTag();
        return true;
    }
}
