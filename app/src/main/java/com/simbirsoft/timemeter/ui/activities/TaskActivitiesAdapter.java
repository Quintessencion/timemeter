package com.simbirsoft.timemeter.ui.activities;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.model.TaskActivityDateItem;
import com.simbirsoft.timemeter.ui.model.TaskActivityItem;
import com.simbirsoft.timemeter.ui.model.TaskActivitySpansItem;
import com.simbirsoft.timemeter.ui.views.TaskActivityItemView;
import com.simbirsoft.timemeter.ui.views.TaskActivityItemsLayout;

import java.util.HashSet;
import java.util.List;

public class TaskActivitiesAdapter extends  RecyclerView.Adapter<TaskActivitiesAdapter.ViewHolder>
                                   implements TaskActivityItemsLayout.TaskActivityItemsAdapter {
    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class DateItemViewHolder extends ViewHolder {
        public DateItemViewHolder(View itemView) {
            super(itemView);
        }
        TextView textView;
    }

    static class SpansItemViewHolder extends ViewHolder {
        public SpansItemViewHolder(View itemView) {
            super(itemView);
        }
        TextView weekDayTextView;
        TextView dateTextView;
        TaskActivityItemsLayout activitiesLayout;
        FrameLayout divider;
    }

    private static final int DATE_ITEM_TYPE = 0;
    private static final int SPANS_ITEM_TYPE = 1;

    private final List<TaskActivityItem> mItems;
    private final HashSet<View> mActivityItemViews;

    public TaskActivitiesAdapter() {
        super();
        mItems = Lists.newArrayList();
        mActivityItemViews = Sets.newHashSet();
    }

    public void setItems(List<TaskActivityItem> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public int getItemViewType (int position) {
        TaskActivityItem item = mItems.get(position);
        return (item instanceof TaskActivitySpansItem) ? SPANS_ITEM_TYPE : DATE_ITEM_TYPE;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
       return (getItemViewType(i) == DATE_ITEM_TYPE) ? createDateItemViewHolder(viewGroup)
               : createSpansItemViewHolder(viewGroup);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == DATE_ITEM_TYPE) {
           bindDateItemViewHolder((DateItemViewHolder)viewHolder, position);
        } else {
            bindSpansItemViewHolder((SpansItemViewHolder) viewHolder, position);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    private ViewHolder createDateItemViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.view_task_activity_date_item, viewGroup, false);

        DateItemViewHolder holder = new DateItemViewHolder(view);
        holder.textView = (TextView)view.findViewById(R.id.textView);
        return holder;
    }

    private ViewHolder createSpansItemViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.view_task_activity_spans_item, viewGroup, false);

        SpansItemViewHolder holder = new SpansItemViewHolder(view);
        holder.weekDayTextView = (TextView)view.findViewById(R.id.weekDaytextView);
        holder.dateTextView = (TextView)view.findViewById(R.id.dateTextView);
        holder.activitiesLayout = (TaskActivityItemsLayout)view.findViewById(R.id.taskActivityItemsLayout);
        holder.divider = (FrameLayout)view.findViewById(R.id.divider);
        holder.activitiesLayout.setAdapter(this);
        return holder;
    }

    private void bindDateItemViewHolder(DateItemViewHolder viewHolder, int position) {
        TaskActivityDateItem item = (TaskActivityDateItem)mItems.get(position);
        viewHolder.textView.setText(item.getDateString());
    }

    private void bindSpansItemViewHolder(SpansItemViewHolder viewHolder, int position) {
        TaskActivitySpansItem item = (TaskActivitySpansItem)mItems.get(position);
        viewHolder.dateTextView.setText(item.getDateString());
        viewHolder.weekDayTextView.setText(item.getWeekDayString());
        viewHolder.activitiesLayout.setTaskActivitySpansItem(item);
        viewHolder.divider.setVisibility(
                (position == getItemCount() - 1) ? View.VISIBLE
                : (getItemViewType(position + 1) == SPANS_ITEM_TYPE)
                ? View.VISIBLE : View.INVISIBLE);
    }

    public View getActivityItemView(TaskActivityItemsLayout layout) {
        View view = null;
        if (mActivityItemViews.size() > 0) {
            view = mActivityItemViews.iterator().next();
            mActivityItemViews.remove(view);
        } else {
            view = LayoutInflater.from(layout.getContext())
                    .inflate(R.layout.view_task_activity_item, layout, false);
            view.setTag(view.findViewById(R.id.taskActivityUtemView));
        }
        return view;
    }

    public void addActivityItemViews(List<View> items) {
        mActivityItemViews.addAll(items);
    }
}
