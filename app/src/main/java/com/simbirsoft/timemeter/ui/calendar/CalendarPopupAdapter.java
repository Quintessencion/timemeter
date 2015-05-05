package com.simbirsoft.timemeter.ui.calendar;

import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andexert.library.RippleView;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.util.ColorSets;

import java.util.List;

public class CalendarPopupAdapter extends RecyclerView.Adapter<CalendarPopupAdapter.ViewHolder>
                                  implements  View.OnClickListener {
    public static interface TaskClickListener {
        void onTaskClicked(TaskBundle item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View mMarker;
        TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mMarker = itemView.findViewById(R.id.popupMarker);
            mTextView = (TextView)itemView.findViewById(R.id.popupTextView);
        }
    }

    private final List<TaskBundle> mItems;
    private TaskClickListener mTaskClickListener;

    public CalendarPopupAdapter() {
        mItems = Lists.newArrayList();
        setHasStableIds(true);
    }

    public void setItems(List<TaskBundle> tasks) {
        mItems.clear();
        mItems.addAll(tasks);
        notifyDataSetChanged();
    }

    public void setTaskClickListener(TaskClickListener taskClickListener) {
        mTaskClickListener = taskClickListener;
    }

    public TaskClickListener getTaskClickListener() {
        return mTaskClickListener;
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).getTask().getId();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.view_calendar_popup_item, viewGroup, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        TaskBundle item = mItems.get(position);
        viewHolder.mTextView.setText(item.getTask().getDescription());
        GradientDrawable drawable = (GradientDrawable)viewHolder.mMarker.getBackground();
        drawable.setColor(ColorSets.getTaskColor(item.getTask().getId()));
        viewHolder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void onClick(View v) {
        if (mTaskClickListener != null) {
            mTaskClickListener.onTaskClicked((TaskBundle)v.getTag());
        }
    }
}
