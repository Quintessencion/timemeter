package com.github.mikephil.charting.data_animation;

/**
 * Created by irinakruglova on 29.01.15.
 */
public interface IChartDataAnimator {
    public static final long DEFAULT_ANIMATION_DURATION = 500;

    public void startAnimation(long duration);

    public void cancelAnimation();

    public boolean isAnimationStarted();

    public void setChartAnimationListener(ChartAnimationListener animationListener);
}
