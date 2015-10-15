package com.simbirsoft.timeactivity.ui.util;

import android.support.v7.widget.RecyclerView;

public class RecyclerViewUtils {
    public static void forwardDataChanges(RecyclerView.Adapter fromAdapter, RecyclerView.Adapter toAdapter) {
        RecyclerView.AdapterDataObserver forwarder = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                toAdapter.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                toAdapter.notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                toAdapter.notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                toAdapter.notifyItemRangeRemoved(positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                toAdapter.notifyDataSetChanged(); //TODO
            }
        };
        fromAdapter.registerAdapterDataObserver(forwarder);
    }
}
