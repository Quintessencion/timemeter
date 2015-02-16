package com.simbirsoft.timemeter.ui.stats;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;

import java.util.Collection;
import java.util.List;

public class StatsListAdapter extends RecyclerView.Adapter<StatsListAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(CardView itemView, View contentView) {
            super(itemView);

            this.itemView = itemView;
            this.contentView = contentView;
        }

        public CardView itemView;
        public View contentView;
    }

    private final List<StatisticsViewBinder> mViewBinders;

    public StatsListAdapter() {
        mViewBinders = Lists.newArrayList();
    }

    public void setViewBinders(Collection<StatisticsViewBinder> binders) {
        mViewBinders.clear();
        mViewBinders.addAll(binders);
        notifyDataSetChanged();
    }

    public StatisticsViewBinder getViewBinder(int position) {
        return mViewBinders.get(position);
    }

    private StatisticsViewBinder getViewBinderForViewTypeId(int viewTypeId) {
        return Iterables.find(mViewBinders, (binder) -> binder.getViewTypeId() == viewTypeId);
    }

    @Override
    public int getItemViewType(int position) {
        return getViewBinder(position).getViewTypeId();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        StatisticsViewBinder binder = getViewBinderForViewTypeId(viewType);

        CardView view = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_statistics_card, parent, false);

        View contentView = binder.createView(parent.getContext(), view);
        view.addView(contentView);

        return new ViewHolder(view, contentView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StatisticsViewBinder binder = getViewBinder(position);

        binder.bindView(holder.contentView);
    }

    @Override
    public int getItemCount() {
        return mViewBinders.size();
    }
}