package com.simbirsoft.timemeter.ui.helpcards;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.util.RecyclerViewUtils;

public class HelpCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements HelpCardPresenter {

    static class HelpCardViewHolder extends RecyclerView.ViewHolder {
        public HelpCardViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static final int VIEW_TYPE_HELP_CARD = 1777;

    private HelpCardSource mHelpCardSource;
    private boolean mNeedToPresentHelpCard;
    private RecyclerView.Adapter mInnerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public HelpCardAdapter(RecyclerView.Adapter adapter) {
        mInnerAdapter = adapter;
        RecyclerViewUtils.forwardDataChanges(mInnerAdapter, this);
        setHasStableIds(mInnerAdapter.hasStableIds());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HELP_CARD) {
            return onCreateHelpCardViewHolder(parent);
        } else {
            return mInnerAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_HELP_CARD) {
            mHelpCardSource.setupHelpCard((HelpCard) holder.itemView);
        } else {
            mInnerAdapter.onBindViewHolder(holder, getDataActualPosition(position));
        }
    }

    @Override
    public int getItemCount() {
        int count = mInnerAdapter.getItemCount();
        return mNeedToPresentHelpCard ? count + 1 : count;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mNeedToPresentHelpCard) ?
                VIEW_TYPE_HELP_CARD : mInnerAdapter.getItemViewType(getDataActualPosition(position));
    }

    @Override
    public long getItemId(int position) {
        return (mNeedToPresentHelpCard && position == 0) ? (1777 + mHelpCardSource.getHelpCardId()) : mInnerAdapter.getItemId(getDataActualPosition(position));
    }

    @Override
    public void show() {
        boolean alreadyPresenting = mNeedToPresentHelpCard;

        mNeedToPresentHelpCard = true;

        if (alreadyPresenting) {
            notifyItemChanged(0);
        } else {
            notifyItemInserted(0);
            scrollToTop();
        }
    }

    @Override
    public void hide() {
        mNeedToPresentHelpCard = false;

        notifyItemRemoved(0);
    }

    private void scrollToTop() {
        if (mLayoutManager != null) {
            mLayoutManager.scrollToPosition(0);
        }
    }

    @Override
    public void setHelpCardSource(HelpCardSource source) {
        mHelpCardSource = source;
    }

    public void setLayoutManager(RecyclerView.LayoutManager lm) {
        mLayoutManager = lm;
    }

    private int getDataActualPosition(int position) {
        return mNeedToPresentHelpCard ? position - 1 : position;
    }

    private HelpCardViewHolder onCreateHelpCardViewHolder(ViewGroup viewGroup) {
        HelpCard helpCard = (HelpCard)LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.view_help_card_item, viewGroup, false);

        HelpCardViewHolder holder = new HelpCardViewHolder(helpCard);

        return holder;
    }
}
