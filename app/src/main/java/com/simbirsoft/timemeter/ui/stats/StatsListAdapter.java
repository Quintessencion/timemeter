package com.simbirsoft.timemeter.ui.stats;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.base.BaseMainPageAdapter;
import com.simbirsoft.timemeter.ui.model.TaskBundle;

import java.util.Collection;
import java.util.List;

public class StatsListAdapter extends BaseMainPageAdapter {

    interface ChartClickListener {
        void onChartClicked(int viewType);
    }

    static class StatsViewHolder extends BaseViewHolder {

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
    protected int internalGetItemViewType(int position) {
        return getViewBinder(position).getViewTypeId();
    }

    @Override
    protected int internalGetItemCount() {
        return mViewBinders.size();
    }

    @Override
    protected long internalGetItemId(int position) {
        return 0;
    }

    @Override
    protected void internalOnBindViewHolder(BaseViewHolder viewHolder, int viewType, int position) {
        StatisticsViewBinder binder = getViewBinder(position);

        binder.bindView(((StatsViewHolder) viewHolder).contentView);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType != VIEW_TYPE_HELP_CARD) {
            StatisticsViewBinder binder = getViewBinderForViewTypeId(viewType);

            CardView view = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_statistics_card, parent, false);

            View contentView = binder.createView(parent.getContext(), view, false);
            view.addView(contentView);
            view.setOnClickListener(mClickListener);
            view.setTag(viewType);

            return new StatsViewHolder(view, contentView);
        }

        return super.onCreateViewHolder(parent, viewType);
    }

    public void setChartClickListener(ChartClickListener chartClickListener) {
        mChartClickListener = chartClickListener;
    }
}
