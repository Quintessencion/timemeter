package com.simbirsoft.timemeter.ui.helpcards;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import jp.wasabeef.recyclerview.animators.ScaleInAnimator;

public class HelpCardAnimator extends ScaleInAnimator {

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        if (notNull(oldHolder)) {
            View oldView = oldHolder.itemView;
            ViewCompat.setScaleX(oldView, 1.0F);
            ViewCompat.setScaleY(oldView, 1.0F);
        }

        if (notNull(newHolder)) {
            View newView = newHolder.itemView;
            ViewCompat.setScaleX(newView, 0.0F);
            ViewCompat.setScaleY(newView, 0.0F);
        }

        animateRemoveOldHolder(oldHolder, newHolder);

        return false;
    }

    private void animateRemoveOldHolder(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder) {
        if (notNull(oldHolder)) {
            ViewCompat.animate(oldHolder.itemView).scaleX(0.0F).scaleY(0.0F).setDuration(this.getRemoveDuration()).setListener(new ViewPropertyAnimatorListener() {
                @Override
                public void onAnimationStart(View view) {
                    dispatchRemoveStarting(oldHolder);
                }

                @Override
                public void onAnimationEnd(View view) {
                    dispatchRemoveFinished(oldHolder);
                    animateAddNewHolder(newHolder);
                }

                @Override
                public void onAnimationCancel(View view) {
                    Log.d("ANIMATE", "REMOVE CANCELLED!!!");
                }
            }).start();
        } else {
            animateAddNewHolder(newHolder);
        }
    }

    private void animateAddNewHolder(RecyclerView.ViewHolder newHolder) {
        if (notNull(newHolder)) {
            ViewCompat.animate(newHolder.itemView).scaleX(1.0F).scaleY(1.0F).setDuration(this.getAddDuration()).setListener(new ViewPropertyAnimatorListener() {
                @Override
                public void onAnimationStart(View view) {
                    dispatchAddStarting(newHolder);
                }

                @Override
                public void onAnimationEnd(View view) {
                    dispatchAddFinished(newHolder);
                    if (!isRunning()) {
                        dispatchAnimationsFinished();
                    }
                }

                @Override
                public void onAnimationCancel(View view) {
                    Log.d("ANIMATE", "ADD CANCELLED!!!");
                }
            }).start();
        }
    }

    private boolean notNull(RecyclerView.ViewHolder viewHolder) {
        return viewHolder != null && viewHolder.itemView != null;
    }
}
