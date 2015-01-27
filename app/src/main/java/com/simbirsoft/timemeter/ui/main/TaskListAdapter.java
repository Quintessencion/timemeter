package com.simbirsoft.timemeter.ui.main;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.controller.ActiveTaskInfo;
import com.simbirsoft.timemeter.controller.ITaskActivityManager;
import com.simbirsoft.timemeter.db.model.Task;

import java.util.Collections;
import java.util.List;
import com.google.common.base.Objects;
import com.simbirsoft.timemeter.ui.util.TimerTextFormatter;


public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    static interface TaskClickListener {
        void onTaskEditClicked(Task item);
        void onTaskCardClicked(Task item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

        View itemEditView;
        TextView titleView;
        Task item;
        TextView timerView;
    }

    private final List<Task> mItems;
    private final ITaskActivityManager mTaskActivityManager;
    private TaskClickListener mTaskClickListener;

    private final View.OnClickListener mCardClickListener =
            view -> {
                if (mTaskClickListener != null) {
                    mTaskClickListener.onTaskCardClicked((Task) view.getTag());
                }
            };

    private final View.OnClickListener mEditClickListener =
            view -> {
                if (mTaskClickListener != null) {
                    mTaskClickListener.onTaskEditClicked((Task) view.getTag());
                }
            };

    public TaskListAdapter(ITaskActivityManager taskActivityManager) {
        mTaskActivityManager = taskActivityManager;
        mItems = Lists.newArrayList();
        setHasStableIds(true);
    }

    public void setItems(List<Task> tasks) {
        mItems.clear();
        mItems.addAll(tasks);
        notifyDataSetChanged();
    }

    public void addFirstItem(Task item) {
        mItems.add(0, item);
        notifyDataSetChanged();
    }

    public void replaceItem(Task item) {
        int index = Iterables.indexOf(mItems, (input) ->
                Objects.equal(input.getId(), item.getId()));

        Preconditions.checkArgument(index > -1, "no item to replace");

        mItems.set(index, item);
        notifyDataSetChanged();
    }

    public List<Task> getItems() {
        return Collections.unmodifiableList(mItems);
    }

    public void removeItems(long taskId) {
        Iterables.removeIf(mItems, (task) -> task.getId() == taskId);
        notifyDataSetChanged();
    }

    public void setTaskClickListener(TaskClickListener taskClickListener) {
        mTaskClickListener = taskClickListener;
    }

    public TaskClickListener getTaskClickListener() {
        return mTaskClickListener;
    }

    public void updateItemView(RecyclerView recyclerView, Task item) {
        ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForItemId(item.getId());
        if (holder == null) {
            return;
        }

        bindViewHolder(holder, item);
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).getId();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                                  .inflate(R.layout.view_task_card, viewGroup, false);

        ViewHolder holder = new ViewHolder(view);

        view.setOnClickListener(mCardClickListener);

        holder.titleView = (TextView) view.findViewById(android.R.id.title);
        holder.timerView = (TextView) view.findViewById(R.id.timerText);
        holder.itemEditView = view.findViewById(android.R.id.edit);
        holder.itemEditView.setOnClickListener(mEditClickListener);

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Task item = mItems.get(position);

        bindViewHolder(viewHolder, item);
    }

    private void bindViewHolder(ViewHolder holder, Task item) {
        holder.titleView.setText(item.getDescription());
        holder.item = item;

        holder.itemView.setTag(item);
        holder.itemEditView.setTag(item);

        if (mTaskActivityManager.isTaskActive(item)) {
            ActiveTaskInfo taskInfo = mTaskActivityManager.getActiveTaskInfo();
            long pastTime = taskInfo.getPastTimeMillis();

            String formattedTime = TimerTextFormatter.formatTaskTimerText(
                    holder.itemView.getResources(), pastTime);
            holder.timerView.setText(Html.fromHtml(formattedTime));

        } else {
            holder.timerView.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

}
