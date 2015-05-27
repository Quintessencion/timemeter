package com.simbirsoft.timemeter.ui.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.simbirsoft.timemeter.ui.views.HelpCard;
import com.simbirsoft.timemeter.ui.views.HelpCardPresenter;
import com.simbirsoft.timemeter.ui.views.HelpCardSource;
import com.simbirsoft.timemeter.ui.views.HelpCard_;

public abstract class BaseMainPageAdapter extends RecyclerView.Adapter<BaseMainPageAdapter.BaseViewHolder> implements HelpCardPresenter {

    public static class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class HelpCardViewHolder extends BaseViewHolder {
        public HelpCardViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static final int VIEW_TYPE_HELP_CARD = 1777;

    private HelpCardSource mHelpCardSource;
    private boolean mNeedToPresentHelpCard;


    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HELP_CARD) {
            return onCreateHelpCardViewHolder(parent);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_HELP_CARD) {
            mHelpCardSource.setupHelpCard((HelpCard) holder.itemView);
        } else {
            internalOnBindViewHolder(holder, viewType, getDataActualPosition(position));
        }
    }

    @Override
    public int getItemCount() {
        int count = internalGetItemCount();
        return mNeedToPresentHelpCard ? count + 1 : count;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mNeedToPresentHelpCard) ?
                VIEW_TYPE_HELP_CARD : internalGetItemViewType(getDataActualPosition(position));
    }

    @Override
    public long getItemId(int position) {
        return (mNeedToPresentHelpCard && position == 0) ? 1777 : internalGetItemId(getDataActualPosition(position));
    }

    @Override
    public void show() {
        mNeedToPresentHelpCard = true;
        notifyDataSetChanged();
    }

    @Override
    public void hide() {
        mNeedToPresentHelpCard = false;
        notifyDataSetChanged();
    }

    @Override
    public void setHelpCardSource(HelpCardSource source) {
        mHelpCardSource = source;
    }

    private int getDataActualPosition(int position) {
        return mNeedToPresentHelpCard ? position - 1 : position;
    }

    protected abstract int internalGetItemViewType(int position);

    protected abstract int internalGetItemCount();

    protected abstract long internalGetItemId(int position);

    protected abstract void internalOnBindViewHolder(BaseViewHolder viewHolder, int viewType, int position);

    private HelpCardViewHolder onCreateHelpCardViewHolder(ViewGroup viewGroup) {
        HelpCard helpCard = HelpCard_.build(viewGroup.getContext());
        HelpCardViewHolder holder = new HelpCardViewHolder(helpCard);
        return holder;
    }
}
