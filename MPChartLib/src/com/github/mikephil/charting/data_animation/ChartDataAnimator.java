package com.github.mikephil.charting.data_animation;

import android.animation.Animator;
import android.animation.ValueAnimator;

/**
 * Created by irinakruglova on 29.01.15.
 */
public class ChartDataAnimator implements IChartDataAnimator, Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    private ValueAnimator animator;
    private final IAnimatedChart chart;
    private ChartAnimationListener animationListener = new DummyChartAnimationListener();

    public ChartDataAnimator(IAnimatedChart chart) {
        this.chart = chart;
        animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.addListener(this);
        animator.addUpdateListener(this);
    }

    @Override
    public void startAnimation(long duration) {
        if (duration >= 0) {
            animator.setDuration(duration);
        } else {
            animator.setDuration(DEFAULT_ANIMATION_DURATION);
        }
        animator.start();
    }

    @Override
    public void cancelAnimation() {
        animator.cancel();
    }

    @Override
    public boolean isAnimationStarted() {
        return animator.isStarted();
    }

    @Override
    public void setChartAnimationListener(ChartAnimationListener animationListener) {
        if (null == animationListener) {
            this.animationListener = new DummyChartAnimationListener();
        } else {
            this.animationListener = animationListener;
        }
    }



    @Override
    public void onAnimationStart(Animator animator) {
        animationListener.onAnimationStarted();
    }

    @Override
    public void onAnimationEnd(Animator animator) {
        chart.animationDataFinished();
        animationListener.onAnimationFinished();
    }

    @Override
    public void onAnimationCancel(Animator animator) {
        //nothing
    }

    @Override
    public void onAnimationRepeat(Animator animator) {
        //nothing
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        chart.animationDataUpdate(valueAnimator.getAnimatedFraction());
    }
}
