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
    private byte[] mOriginalState;

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

    public TaskBundle createOriginalBundle() {
        if (mOriginalState == null) {
            throw new IllegalStateException("state is not persisted");
        }

        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(mOriginalState, 0, mOriginalState.length);
        parcel.setDataPosition(0);
        TaskBundle bundle = parcel.readParcelable(getClass().getClassLoader());
        parcel.recycle();

        return bundle;
    }

    protected TaskBundle(Parcel parcel) {
        readParcel(parcel);
    }

    public boolean hasPersistedState() {
        return mOriginalState != null;
    }

    public void persistState() {
        persistStateFrom(this);
    }

    private void persistStateFrom(TaskBundle bundle) {
        Parcel parcel = Parcel.obtain();
        parcel.writeParcelable(bundle, 0);
        mOriginalState = parcel.marshall();
        parcel.recycle();
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
        final ClassLoader classLoader = getClass().getClassLoader();
        mTask = parcel.readParcelable(classLoader);
        Parcelable[] parcelables = parcel.readParcelableArray(getClass().getClassLoader());

        mTags = Lists.newArrayListWithCapacity(parcelables.length);
        for (Parcelable parcelable : parcelables) {
            mTags.add((Tag) parcelable);
        }

        boolean hasPersistedState = parcel.readByte() == 1;
        if (hasPersistedState) {
            int length = parcel.readInt();
            mOriginalState = new byte[length];
            parcel.readByteArray(mOriginalState);
        }
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(mTask, 0);
        Tag[] tags = mTags.toArray(new Tag[mTags.size()]);
        parcel.writeParcelableArray(tags, 0);

        if (hasPersistedState()) {
            parcel.writeByte((byte)1);
            parcel.writeInt(mOriginalState.length);
            parcel.writeByteArray(mOriginalState);
        } else {
            parcel.writeByte((byte)0);
        }
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
