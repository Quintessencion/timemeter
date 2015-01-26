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

    public TaskClickListener getTaskClickListener() {
        return mTaskClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

        View itemEditView;
        TextView titleView;
        Task item;
    }

    private final List<Task> mTasks;
    private TaskClickListener mTaskClickListener;

    private final View.OnClickListener mEditClickListener =
            view -> {
                if (mTaskClickListener != null) {
                    mTaskClickListener.onTaskEditClicked((Task) view.getTag());
                }
            };

    public TaskListAdapter() {
        mTasks = Lists.newArrayList();
    }

    public void setTasks(List<Task> tasks) {
        mTasks.clear();
        mTasks.addAll(tasks);
        notifyDataSetChanged();
    }

    public void setTaskClickListener(TaskClickListener taskClickListener) {
        mTaskClickListener = taskClickListener;
    }

    static interface TaskClickListener {
        void onTaskEditClicked(Task item);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                                  .inflate(R.layout.view_task_card, viewGroup, false);

        ViewHolder holder = new ViewHolder(view);

        holder.titleView = (TextView) view.findViewById(android.R.id.title);
        holder.itemEditView = view.findViewById(android.R.id.edit);
        holder.itemEditView.setOnClickListener(mEditClickListener);

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Task item = mTasks.get(i);
        viewHolder.titleView.setText(item.getDescription());
        viewHolder.item = item;
        viewHolder.itemEditView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

}
