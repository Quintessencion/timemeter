package com.simbirsoft.timeactivity.ui.base;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.View;

import jp.wasabeef.recyclerview.animators.holder.AnimateViewHolder;


public class BaseAnimateViewHolder extends AnimateViewHolder {

    public BaseAnimateViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void preAnimateAddImpl() {
        ViewCompat.setAlpha(this.itemView, 0);
    }

    @Override
    public void preAnimateRemoveImpl() {
        ViewCompat.setAlpha(this.itemView, 1);
    }

    @Override
    public void animateAddImpl(ViewPropertyAnimatorListener viewPropertyAnimatorListener) {
        ViewCompat.animate(this.itemView).alpha(1).setDuration(120).setListener(viewPropertyAnimatorListener).start();
    }

    @Override
    public void animateRemoveImpl(ViewPropertyAnimatorListener viewPropertyAnimatorListener) {
        ViewCompat.animate(this.itemView).alpha(0).setDuration(120).setListener(viewPropertyAnimatorListener).start();
    }
}
