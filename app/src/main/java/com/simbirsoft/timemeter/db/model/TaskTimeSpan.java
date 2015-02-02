package com.simbirsoft.timemeter.db.model;

import nl.qbusict.cupboard.annotation.Column;

public class TaskTimeSpan {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_TASK_ID = "task_id";
    public static final String COLUMN_START_TIME = "start_time";
    public static final String COLUMN_END_TIME = "end_time";
    public static final String COLUMN_IS_ACTIVE = "is_active";

    @Column(COLUMN_ID)
    private Long _id;

    @Column(COLUMN_TASK_ID)
    private Long taskId;

    @Column(COLUMN_DESCRIPTION)
    private String description;

    @Column(COLUMN_START_TIME)
    private long startTimeMillis;

    @Column(COLUMN_END_TIME)
    private long endTimeMillis;

    @Column(COLUMN_IS_ACTIVE)
    private boolean isActive;

    public Long getId() {
        return _id;
    }

    public void setId(Long id) {
        this._id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public void setEndTimeMillis(long endTimeMillis) {
        this.endTimeMillis = endTimeMillis;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
