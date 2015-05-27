package com.simbirsoft.timemeter.ui.tasklist;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.controller.ActiveTaskInfo;
import com.simbirsoft.timemeter.controller.ITaskActivityManager;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseMainPageAdapter;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.util.TimerTextFormatter;
import com.simbirsoft.timemeter.ui.views.TagFlowView;
import com.simbirsoft.timemeter.ui.views.TagView;

import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;

public class TaskListAdapter extends BaseMainPageAdapter {

    interface TaskClickListener {
        void onTaskViewClicked(TaskBundle item);
        void onTaskViewLongClicked(TaskBundle item, View itemView);
        void onTaskCardClicked(TaskBundle item);
    }

    static class TaskViewHolder extends BaseViewHolder {
        public TaskViewHolder(View itemView) {
            super(itemView);
        }

        View itemEditView;
        TextView titleView;
        TaskBundle item;
        TextView timerView;
        TagFlowView tagFlowView;
    }

    private static final int VIEW_TYPE_TASK = 1;

    private static final Logger LOG = LogFactory.getLogger(TaskListAdapter.class);

    private final List<TaskBundle> mItems;
    private final ITaskActivityManager mTaskActivityManager;
    private TaskClickListener mTaskClickListener;

    private final View.OnClickListener mCardClickListener = (view) -> {
        if (mTaskClickListener != null) {
            mTaskClickListener.onTaskCardClicked((TaskBundle) view.getTag());
        }
    };

    private final View.OnClickListener mViewClickListener = (view) -> {
        if (mTaskClickListener != null) {
            mTaskClickListener.onTaskViewClicked((TaskBundle) view.getTag());
        }
    };

    private final View.OnLongClickListener mViewLongClickListener = (view) -> {
        if (mTaskClickListener != null) {
            mTaskClickListener.onTaskViewLongClicked((TaskBundle) view.getTag(), view);
            return true;
        }
        return false;
    };

    private final TagView.TagViewClickListener mTagViewClickListener = tagView -> {
        LOG.debug("Tag <" + tagView.getTag().getName() + "> clicked!");
    };

    public TaskListAdapter(ITaskActivityManager taskActivityManager) {
        mTaskActivityManager = taskActivityManager;
        mItems = Lists.newArrayList();
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

        if (index < 0) {
            mItems.add(item);
        } else {
            mItems.set(index, item);
        }

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
        TaskViewHolder holder = (TaskViewHolder) recyclerView.findViewHolderForItemId(task.getId());
        if (holder == null) {
            return;
        }

        bindViewHolder(holder, holder.item);
    }

    @Override
    protected long internalGetItemId(int position) {
        return mItems.get(position).getTask().getId();
    }

    @Override
    protected int internalGetItemViewType(int position) {
        return VIEW_TYPE_TASK;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_TYPE_TASK) {
            return onCreateTaskItemViewHolder(viewGroup);
        }

        return super.onCreateViewHolder(viewGroup, viewType);
    }

    private TaskViewHolder onCreateTaskItemViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.view_task_card, viewGroup, false);

        TaskViewHolder holder = new TaskViewHolder(view);

        view.setOnClickListener(mCardClickListener);

        holder.titleView = (TextView) view.findViewById(android.R.id.title);
        holder.timerView = (TextView) view.findViewById(R.id.timerText);
        holder.tagFlowView = (TagFlowView) view.findViewById(R.id.tagFlowView);

        holder.itemEditView = view.findViewById(R.id.edit_or_view);
        holder.itemEditView.setOnClickListener(mViewClickListener);
        holder.itemEditView.setOnLongClickListener(mViewLongClickListener);

        return holder;
    }

    @Override
    protected void internalOnBindViewHolder(BaseViewHolder viewHolder, int viewType, int position) {
        if (viewType == VIEW_TYPE_TASK) {
            TaskBundle item = mItems.get(position);
            bindViewHolder((TaskViewHolder)viewHolder, item);
        }
    }

    private void bindViewHolder(TaskViewHolder holder, TaskBundle item) {
        final Task task = item.getTask();

        holder.titleView.setText(task.getDescription());
        holder.item = item;

        holder.itemView.setTag(item);
        holder.itemEditView.setTag(item);

        holder.tagFlowView.bindTagViews(item.getTags());
        holder.tagFlowView.setTagViewsClickListener(mTagViewClickListener);

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
    protected int internalGetItemCount() {
        return mItems.size();
    }

    public int getTaskCount() {
        return internalGetItemCount();
    }
}
