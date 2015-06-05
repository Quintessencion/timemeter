package com.simbirsoft.timemeter.db.model;

import android.os.Parcel;
import android.os.Parcelable;

import nl.qbusict.cupboard.annotation.Column;

public class DemoTask implements Parcelable {
    public static final String TABLE_NAME = "DemoTask";
    public static final String COLUMN_ID = "_id";

    public static final Parcelable.Creator<DemoTask> CREATOR =
            new Parcelable.Creator<DemoTask>() {
                @Override
                public DemoTask createFromParcel(Parcel parcel) {
                    return new DemoTask(parcel);
                }

                @Override
                public DemoTask[] newArray(int sz) {
                    return new DemoTask[sz];
                }
            };

    @Column(COLUMN_ID)
    private Long _id;

    public DemoTask() {
    }

    public DemoTask(Task other) {
        _id = other.getId();
    }

    protected DemoTask(Parcel source) {
        readParcel(source);
    }

    public Long getId() {
        return _id;
    }

    public void setId(Long _id) {
        this._id = _id;
    }

    public boolean hasId() {
        return _id != null;
    }

    @Override
    public String toString() {
        return "Task{" +
                "_id=" + _id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DemoTask task = (DemoTask) o;

        if (_id != null ? !_id.equals(task._id) : task._id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _id != null ? _id.hashCode() : 0;
        return result;
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
    }

    private void readParcel(Parcel parcel) {
        boolean hasId = parcel.readByte() == 1;
        if (hasId) {
            _id = parcel.readLong();
        }
    }
}
