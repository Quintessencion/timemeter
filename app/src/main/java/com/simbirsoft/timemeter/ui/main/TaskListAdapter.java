package com.simbirsoft.timemeter.ui.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Task;

import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

        TextView titleView;
    }

    private final List<Task> mTasks;

    public TaskListAdapter() {
        mTasks = Lists.newArrayList();
    }

    public void setTasks(List<Task> tasks) {
        mTasks.clear();
        mTasks.addAll(tasks);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                                  .inflate(R.layout.view_task_card, viewGroup, false);

        ViewHolder holder = new ViewHolder(view);

        holder.titleView = (TextView) view.findViewById(android.R.id.title);

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Task item = mTasks.get(i);
        viewHolder.titleView.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

}
