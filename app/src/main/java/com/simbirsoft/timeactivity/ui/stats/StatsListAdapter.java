package com.simbirsoft.timeactivity.ui.stats;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.ui.base.BaseAnimateViewHolder;

import java.util.Collection;
import java.util.List;

public class StatsListAdapter extends RecyclerView.Adapter<StatsListAdapter.StatsViewHolder> {

    interface ChartClickListener {
        void onChartClicked(int viewType);
    }

    static class StatsViewHolder extends BaseAnimateViewHolder {

        public StatsViewHolder(CardView itemView, View contentView) {
            super(itemView);

            this.itemView = itemView;
            this.contentView = contentView;
        }

        public CardView itemView;
        public View contentView;
    }

    private final List<StatisticsViewBinder> mViewBinders;
    private ChartClickListener mChartClickListener;

    private final View.OnClickListener mClickListener =
            view -> {
                if (mChartClickListener != null) {
                    mChartClickListener.onChartClicked((int)view.getTag());
                }
            };

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
    public int getItemCount() {
        return mViewBinders.size();
    }

    @Override
    public long getItemId(int position) {
        return getViewBinder(position).getViewTypeId();
    }

    @Override
    public void onBindViewHolder(StatsViewHolder viewHolder, int position) {
        StatisticsViewBinder binder = getViewBinder(position);

        binder.bindView(viewHolder.contentView);
    }

    @Override
    public StatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        StatisticsViewBinder binder = getViewBinderForViewTypeId(viewType);

        CardView view = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_statistics_card, parent, false);

        View contentView = binder.createView(parent.getContext(), view, false);
        view.addView(contentView);
        view.setOnClickListener(mClickListener);
        view.setTag(viewType);

        return new StatsViewHolder(view, contentView);
    }

    public void setChartClickListener(ChartClickListener chartClickListener) {
        mChartClickListener = chartClickListener;
    }
}
