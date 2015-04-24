package com.simbirsoft.timemeter.ui.activities;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.model.TaskActivityDateItem;
import com.simbirsoft.timemeter.ui.model.TaskActivityItem;
import com.simbirsoft.timemeter.ui.model.TaskActivityListItem;

import java.util.List;

public class TaskActivitiesAdapter extends  RecyclerView.Adapter<TaskActivitiesAdapter.ViewHolder> {
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

    static class ListItemViewHolder extends ViewHolder {
        public ListItemViewHolder(View itemView) {
            super(itemView);
        }
        TextView weekDayTextView;
        TextView dateTextView;
    }


    private final List<TaskActivityItem> mItems;

    public TaskActivitiesAdapter() {
        super();
        mItems = Lists.newArrayList();
    }

    public void setItems(List<TaskActivityItem> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public int getItemViewType (int position) {
        TaskActivityItem item = mItems.get(position);
        return (item instanceof TaskActivityListItem) ? 1 : 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
       return (getItemViewType(i) == 0) ? createDateItemViewHolder(viewGroup)
               :createListItemViewHolder(viewGroup);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == 0) {
           bindDateItemViewHolder((DateItemViewHolder)viewHolder, position);
        } else {
            bindListItemViewHolder((ListItemViewHolder) viewHolder, position);
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

    private ViewHolder createListItemViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.view_task_activity_list_item, viewGroup, false);

        ListItemViewHolder holder = new ListItemViewHolder(view);
        holder.weekDayTextView = (TextView)view.findViewById(R.id.weekDaytextView);
        holder.dateTextView = (TextView)view.findViewById(R.id.dateTextView);
        return holder;
    }

    private void bindDateItemViewHolder(DateItemViewHolder viewHolder, int position) {
        TaskActivityDateItem item = (TaskActivityDateItem)mItems.get(position);
        viewHolder.textView.setText(item.getDateString());
    }

    private void bindListItemViewHolder(ListItemViewHolder viewHolder, int position) {
        TaskActivityListItem item = (TaskActivityListItem)mItems.get(position);
        viewHolder.weekDayTextView.setText(item.getWeekDayString());
        viewHolder.dateTextView.setText(item.getDateString());
    }
}
