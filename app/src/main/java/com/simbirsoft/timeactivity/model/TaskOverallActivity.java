package com.simbirsoft.timeactivity.model;

import com.simbirsoft.timeactivity.db.model.Task;

import nl.qbusict.cupboard.annotation.Column;
import nl.qbusict.cupboard.annotation.Ignore;

/**
 * Class used in conjunction with {@link com.simbirsoft.timeactivity.db.TaskOverallTimeEntityConverter}
 * to join task with overall activity time.
 *
 * Model is NOT stored in database.
 */
public final class TaskOverallActivity extends Task {

    public static final String COLUMN_OVERALL_DURATION = "duration";

    @Column(COLUMN_OVERALL_DURATION)
    private Long duration;

    @Ignore
    private float durationRatio;

    public TaskOverallActivity() {
    }

    public TaskOverallActivity(Task task) {
        super(task);
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public float getDurationRatio() {
        return durationRatio;
    }

    public void setDurationRatio(float durationRatio) {
        this.durationRatio = durationRatio;
    }
}
