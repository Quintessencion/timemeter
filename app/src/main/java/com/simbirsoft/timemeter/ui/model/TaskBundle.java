package com.simbirsoft.timemeter.ui.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;

import java.util.List;


public class TaskBundle implements Parcelable {

    public static final Parcelable.Creator<TaskBundle> CREATOR =
            new Parcelable.Creator<TaskBundle>() {
                @Override
                public TaskBundle createFromParcel(Parcel parcel) {
                    return new TaskBundle(parcel);
                }

                @Override
                public TaskBundle[] newArray(int sz) {
                    return new TaskBundle[sz];
                }
            };

    private Task mTask;
    private List<Tag> mTags;
    private int mSavedState;

    public static TaskBundle create(Task task, List<Tag> tags) {
        TaskBundle bundle = new TaskBundle();
        bundle.setTags(tags);
        bundle.setTask(task);

        return bundle;
    }

    public static TaskBundle create() {
        Task task = new Task();
        task.setDescription("");

        return create(task, Lists.newArrayList());
    }

    public TaskBundle() {
    }

    protected TaskBundle(Parcel parcel) {
        readParcel(parcel);
    }

    public void saveState() {
        mSavedState = hashCode();
    }

    public boolean isEqualToSavedState() {
        return mSavedState == hashCode();
    }

    public Task getTask() {
        return mTask;
    }

    public void setTask(Task task) {
        mTask = task;
    }

    public List<Tag> getTags() {
        return mTags;
    }

    public void setTags(List<Tag> tags) {
        mTags = tags;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void readParcel(Parcel parcel) {
        mTask = parcel.readParcelable(getClass().getClassLoader());
        Parcelable[] parcelables = parcel.readParcelableArray(getClass().getClassLoader());

        mTags = Lists.newArrayListWithCapacity(parcelables.length);
        for (Parcelable parcelable : parcelables) {
            mTags.add((Tag) parcelable);
        }
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(mTask, 0);
        Tag[] tags = mTags.toArray(new Tag[mTags.size()]);
        parcel.writeParcelableArray(tags, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskBundle that = (TaskBundle) o;

        if (mTags != null ? !mTags.equals(that.mTags) : that.mTags != null) return false;
        if (mTask != null ? !mTask.equals(that.mTask) : that.mTask != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mTask != null ? mTask.hashCode() : 0;
        result = 31 * result + (mTags != null ? mTags.hashCode() : 0);
        return result;
    }
}
