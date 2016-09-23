package com.simbirsoft.timeactivity.ui.helpcards;

import android.animation.Animator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import jp.wasabeef.recyclerview.animators.ScaleInAnimator;

public class HelpCardAnimator extends ScaleInAnimator {

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        if (notNull(oldHolder)) {
            View oldView = oldHolder.itemView;
            oldView.setScaleX(1.0F);
            oldView.setScaleY(1.0F);
        }

        if (notNull(newHolder)) {
            View newView = newHolder.itemView;
            newView.setScaleX(0.0F);
            newView.setScaleY(0.0F);
        }

        animateRemoveOldHolder(oldHolder, newHolder);

        return false;
    }

    private void animateRemoveOldHolder(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder) {
        if (notNull(oldHolder)) {
            oldHolder.itemView.animate().scaleX(0.0F).scaleY(0.0F).setDuration(this.getRemoveDuration()).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    dispatchRemoveStarting(oldHolder);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    dispatchRemoveFinished(oldHolder);
                    animateAddNewHolder(newHolder);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    Log.d("ANIMATE", "REMOVE CANCELLED!!!");
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
        } else {
            animateAddNewHolder(newHolder);
        }
    }

    private void animateAddNewHolder(RecyclerView.ViewHolder newHolder) {
        if (notNull(newHolder)) {
            newHolder.itemView.animate().scaleX(1.0F).scaleY(1.0F).setDuration(this.getAddDuration()).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    dispatchAddStarting(newHolder);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    dispatchAddFinished(newHolder);
                    if (!isRunning()) {
                        dispatchAnimationsFinished();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    Log.d("ANIMATE", "ADD CANCELLED!!!");
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
        }
    }

    private boolean notNull(RecyclerView.ViewHolder viewHolder) {
        return viewHolder != null && viewHolder.itemView != null;
    }
}
