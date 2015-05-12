package com.simbirsoft.timemeter.ui.calendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.util.ColorSets;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CalendarPopupAdapter extends RecyclerView.Adapter<CalendarPopupAdapter.ViewHolder>
                                  implements  View.OnClickListener {
    public static interface TaskClickListener {
        void onTaskClicked(TaskBundle item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View mMarker;
        TextView mTextView;
        RelativeLayout mLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            mMarker = itemView.findViewById(R.id.popupMarker);
            mTextView = (TextView)itemView.findViewById(R.id.popupTextView);
            mLayout = (RelativeLayout)itemView.findViewById(R.id.popupLayout);
        }
    }

    private final List<TaskBundle> mItems;
    private TaskClickListener mTaskClickListener;
    private int mMiddleItemPadding;
    private int mFirstItemPadding;
    private int mLastItemPadding;

    public CalendarPopupAdapter(Context context) {
        mItems = Lists.newArrayList();
        setHasStableIds(true);
        final Resources res = context.getResources();
        mMiddleItemPadding = res.getDimensionPixelSize(R.dimen.calendar_popup_middle_item_padding);
        mFirstItemPadding = res.getDimensionPixelSize(R.dimen.calendar_popup_first_item_padding);
        mLastItemPadding = res.getDimensionPixelSize(R.dimen.calendar_popup_last_item_padding);
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
        viewHolder.itemView.setPadding(viewHolder.itemView.getPaddingLeft(),
                (position == 0) ? mFirstItemPadding : mMiddleItemPadding,
                viewHolder.itemView.getPaddingRight(),
                (position == getItemCount() - 1) ? mLastItemPadding : mMiddleItemPadding);
        viewHolder.mLayout.setTag(item);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void onClick(View v) {
        if (mTaskClickListener != null) {
            mTaskClickListener.onTaskClicked((TaskBundle) v.getTag());
        }
    }
}
