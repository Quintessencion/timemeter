package com.simbirsoft.timemeter.ui.calendar;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.andexert.library.RippleView;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.util.ColorSets;

import java.util.Collections;
import java.util.List;

public class CalendarPopupAdapter extends RecyclerView.Adapter<CalendarPopupAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
        View marker;
        TextView textView;
    }

    private final List<TaskBundle> mItems;

    public CalendarPopupAdapter() {
        mItems = Lists.newArrayList();
        setHasStableIds(true);
    }

    public void setItems(List<TaskBundle> tasks) {
        mItems.clear();
        mItems.addAll(tasks);
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

        ViewHolder holder = new ViewHolder(view);
        holder.marker = view.findViewById(R.id.popupMarker);
        holder.textView = (TextView)view.findViewById(R.id.popupTextView);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        TaskBundle item = mItems.get(position);
        viewHolder.marker.setBackgroundColor(ColorSets.getTaskColor(item.getTask().getId()));
        viewHolder.textView.setText(item.getTask().getDescription());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
