package com.simbirsoft.timemeter.ui.tasklist;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.controller.ActiveTaskInfo;
import com.simbirsoft.timemeter.controller.ITaskActivityManager;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.util.TagViewUtils;
import com.simbirsoft.timemeter.ui.util.TimerTextFormatter;

import org.apmem.tools.layouts.FlowLayout;

import java.util.Collections;
import java.util.List;
import java.util.Stack;


public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    static interface TaskClickListener {
        void onTaskViewClicked(TaskBundle item);
        void onTaskViewLongClicked(TaskBundle item, View itemView);
        void onTaskCardClicked(TaskBundle item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

        View itemEditView;
        TextView titleView;
        TaskBundle item;
        TextView timerView;
        FlowLayout tagContainerView;
    }

    private final List<TaskBundle> mItems;
    private final ITaskActivityManager mTaskActivityManager;
    private final Stack<View> mReuseTagViews;
    private TaskClickListener mTaskClickListener;

    private final View.OnClickListener mCardClickListener =
            view -> {
                if (mTaskClickListener != null) {
                    mTaskClickListener.onTaskCardClicked((TaskBundle) view.getTag());
                }
            };

    private final View.OnClickListener mViewClickListener =
            view -> {
                if (mTaskClickListener != null) {
                    mTaskClickListener.onTaskViewClicked((TaskBundle) view.getTag());
                }
            };

    private final View.OnLongClickListener mViewLongClickListener =
            view -> {
                if (mTaskClickListener != null) {
                    mTaskClickListener.onTaskViewLongClicked((TaskBundle) view.getTag(), view);
                    return true;
                }

                return false;
            };

    public TaskListAdapter(ITaskActivityManager taskActivityManager) {
        mTaskActivityManager = taskActivityManager;
        mItems = Lists.newArrayList();
        mReuseTagViews = new Stack<>();
        setHasStableIds(true);
    }

    public void setItems(List<TaskBundle> tasks) {
        mItems.clear();
        mItems.addAll(tasks);
        notifyDataSetChanged();
    }

    public void addFirstItem(TaskBundle item) {
        mItems.add(0, item);
        notifyDataSetChanged();
    }

    public void replaceItem(TaskBundle item) {
        int index = Iterables.indexOf(mItems, (input) ->
                Objects.equal(input.getTask().getId(), item.getTask().getId()));

        Preconditions.checkArgument(index > -1, "no item to replace");

        mItems.set(index, item);
        notifyDataSetChanged();
    }

    public List<TaskBundle> getItems() {
        return Collections.unmodifiableList(mItems);
    }

    public void removeItems(long taskId) {
        Iterables.removeIf(mItems, (task) -> task.getTask().getId() == taskId);
        notifyDataSetChanged();
    }

    public void setTaskClickListener(TaskClickListener taskClickListener) {
        mTaskClickListener = taskClickListener;
    }

    public TaskClickListener getTaskClickListener() {
        return mTaskClickListener;
    }

    public void updateItemView(RecyclerView recyclerView, Task task) {
        ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForItemId(task.getId());
        if (holder == null) {
            return;
        }

        bindViewHolder(holder, holder.item);
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).getTask().getId();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                                  .inflate(R.layout.view_task_card, viewGroup, false);

        ViewHolder holder = new ViewHolder(view);

        view.setOnClickListener(mCardClickListener);

        holder.titleView = (TextView) view.findViewById(android.R.id.title);
        holder.timerView = (TextView) view.findViewById(R.id.timerText);
        holder.tagContainerView = (FlowLayout) view.findViewById(R.id.tagViewContainer);

        holder.itemEditView = view.findViewById(R.id.edit_or_view);
        holder.itemEditView.setOnClickListener(mViewClickListener);
        holder.itemEditView.setOnLongClickListener(mViewLongClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        TaskBundle item = mItems.get(position);

        bindViewHolder(viewHolder, item);
    }

    private void bindTagViews(ViewGroup tagLayout, List<Tag> tags) {
        final int tagCount = tags.size();
        final View[] reuseViews = new View[tagCount];

        final int reuseViewCount = tagLayout.getChildCount();
        for (int i = 0; i < reuseViewCount; i++) {
            mReuseTagViews.add(tagLayout.getChildAt(i));
        }
        tagLayout.removeAllViewsInLayout();

        for (int i = 0; i < tagCount; i++) {
            if (mReuseTagViews.isEmpty()) {
                reuseViews[i] = TagViewUtils.inflateTagView(
                        LayoutInflater.from(tagLayout.getContext()),
                        tagLayout,
                        0);
            } else {
                reuseViews[i] = mReuseTagViews.pop();
            }

            tagLayout.addView(reuseViews[i]);
        }

        if (tagCount > 0) {
            for (int i = 0; i < tagCount; i++) {
                Tag tag = tags.get(i);
                TextView tagView = (TextView) reuseViews[i];
                tagView.setText(tag.getName());
                TagViewUtils.updateTagViewColor(tagView, tag.getColor());
            }
            tagLayout.setVisibility(View.VISIBLE);
        } else {
            tagLayout.setVisibility(View.GONE);
        }
    }

    private void bindViewHolder(ViewHolder holder, TaskBundle item) {
        final Task task = item.getTask();

        holder.titleView.setText(task.getDescription());
        holder.item = item;

        holder.itemView.setTag(item);
        holder.itemEditView.setTag(item);

        bindTagViews(holder.tagContainerView, item.getTags());

        if (mTaskActivityManager.isTaskActive(task)) {
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
