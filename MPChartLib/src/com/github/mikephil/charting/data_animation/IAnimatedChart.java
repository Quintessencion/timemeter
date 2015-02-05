package com.github.mikephil.charting.data_animation;

/**
 * Created by irinakruglova on 29.01.15.
 */
public interface IAnimatedChart {
    /**
     * Updates chart data with given scale. Called during chart data animation update.
     */
    public void animationDataUpdate(float scale);

    /**
     * Called when data animation finished.
     */
    public void animationDataFinished();

    /**
     * Starts chart data animation for given duration. Before you call this method you should change target values of
     * chart data.
     */
    public void startDataAnimation();

    /**
     * Starts chart data animation for given duration. If duration is negative the default value of 500ms will be used.
     * Before you call this method you should change target values of chart data.
     */
    public void startDataAnimation(long duration);

    /**
     * Stops chart data animation. All chart data values are set to their target values.
     */
    public void cancelDataAnimation();
}
