package com.simbirsoft.timemeter.db.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collection;

import nl.qbusict.cupboard.annotation.Column;
import nl.qbusict.cupboard.annotation.Ignore;
import nl.qbusict.cupboard.annotation.Index;

public class TaskTimeSpan implements Parcelable {

    public static final String TABLE_NAME = "TaskTimeSpan";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_TASK_ID = "task_id";
    public static final String COLUMN_START_TIME = "start_time";
    public static final String COLUMN_END_TIME = "end_time";
    public static final String COLUMN_IS_ACTIVE = "is_active";

    public static final Creator<TaskTimeSpan> CREATOR =
            new Creator<TaskTimeSpan>() {
                @Override
                public TaskTimeSpan createFromParcel(Parcel parcel) {
                    return new TaskTimeSpan(parcel);
                }

                @Override
                public TaskTimeSpan[] newArray(int sz) {
                    return new TaskTimeSpan[sz];
                }
            };

    @Column(COLUMN_ID)
    private Long _id;

    @Column(COLUMN_TASK_ID)
    @Index
    private Long taskId;

    @Column(COLUMN_DESCRIPTION)
    @Index
    private String description;

    @Column(COLUMN_START_TIME)
    private long startTimeMillis;

    @Column(COLUMN_END_TIME)
    private long endTimeMillis;

    @Column(COLUMN_IS_ACTIVE)
    private boolean isActive;

    @Ignore
    private float durationRatio;

    public TaskTimeSpan() {
    }

    private TaskTimeSpan(Parcel source) {
        if (source.readByte() == 1) {
            _id = source.readLong();
        }
        if (source.readByte() == 1) {
            taskId = source.readLong();
        }
        description = source.readString();
        startTimeMillis = source.readLong();
        endTimeMillis = source.readLong();
        isActive = source.readByte() == 1;
    }

    public boolean hasId() {
        return _id != null;
    }

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

    public long getDuration() {
        return endTimeMillis - startTimeMillis;
    }

    public float getDurationRatio() {
        return durationRatio;
    }

    public void setDurationRatio(float durationRatio) {
        this.durationRatio = durationRatio;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (hasId() ? 1 : 0));
        if (hasId()) {
            parcel.writeLong(_id);
        }
        boolean hasTaskId = taskId != null;
        parcel.writeByte((byte) (hasTaskId ? 1 : 0));
        if (hasTaskId) {
            parcel.writeLong(taskId);
        }

        parcel.writeString(description);
        parcel.writeLong(startTimeMillis);
        parcel.writeLong(endTimeMillis);
        parcel.writeByte((byte) (isActive ? 1 : 0));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof TaskTimeSpan) {
            return _id.equals(((TaskTimeSpan)o)._id);
        }
        return false;
    }

    public boolean isOverlaps(TaskTimeSpan span) {
        return (this.startTimeMillis <= span.endTimeMillis) && (span.startTimeMillis <= this.endTimeMillis);
    }

    public boolean isOverlaps(Collection<TaskTimeSpan> spans) {
        for(TaskTimeSpan span : spans) {
            if (isOverlaps(span)) {
                return true;
            }
        }
        return false;
    }
}
