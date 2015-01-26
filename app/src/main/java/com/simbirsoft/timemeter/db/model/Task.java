package com.simbirsoft.timemeter.db.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Task implements Parcelable {

    public static final Parcelable.Creator<Task> CREATOR =
            new Parcelable.Creator<Task>() {
                @Override
                public Task createFromParcel(Parcel parcel) {
                    return new Task(parcel);
                }

                @Override
                public Task[] newArray(int sz) {
                    return new Task[sz];
                }
            };

    private Long _id;
    private String description;
    private Date createDate;

    public Task() {
    }

    protected Task(Parcel source) {
        readParcel(source);
    }

    public Long getId() {
        return _id;
    }

    public void setId(Long _id) {
        this._id = _id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "Task{" +
                "_id=" + _id +
                ", description='" + description + '\'' +
                ", createDate=" + createDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        if (_id != null ? !_id.equals(task._id) : task._id != null) return false;
        if (createDate != null ? !createDate.equals(task.createDate) : task.createDate != null)
            return false;
        if (description != null ? !description.equals(task.description) : task.description != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _id != null ? _id.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (createDate != null ? createDate.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(_id);
        parcel.writeString(description);
        parcel.writeLong(createDate.getTime());
    }

    private void readParcel(Parcel parcel) {
        _id = parcel.readLong();
        description = parcel.readString();
        createDate = new Date(parcel.readLong());
    }
}
