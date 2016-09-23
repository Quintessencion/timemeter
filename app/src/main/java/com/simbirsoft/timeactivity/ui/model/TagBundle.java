package com.simbirsoft.timeactivity.ui.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.Lists;
import com.simbirsoft.timeactivity.db.model.Tag;
import com.simbirsoft.timeactivity.db.model.TaskTag;

import java.util.List;


public class TagBundle implements Parcelable {

    public static final Creator<TagBundle> CREATOR =
            new Creator<TagBundle>() {
                @Override
                public TagBundle createFromParcel(Parcel parcel) {
                    return new TagBundle(parcel);
                }

                @Override
                public TagBundle[] newArray(int sz) {
                    return new TagBundle[sz];
                }
            };

    private Tag mTag;
    private List<TaskTag> mTaskTags;

    public static TagBundle create(Tag tag, List<TaskTag> taskTags) {
        TagBundle bundle = new TagBundle();
        bundle.setTag(tag);
        bundle.setTaskTags(taskTags);

        return bundle;
    }

    public TagBundle() {
    }

    protected TagBundle(Parcel parcel) {
        readParcel(parcel);
    }

    public Tag getTag() {
        return mTag;
    }

    public void setTag(Tag tag) {
        mTag = tag;
    }

    public List<TaskTag> getTaskTags() {
        return mTaskTags;
    }

    public void setTaskTags(List<TaskTag> taskTags) {
        mTaskTags = taskTags;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void readParcel(Parcel parcel) {
        mTag = parcel.readParcelable(getClass().getClassLoader());
        Parcelable[] parcelables = parcel.readParcelableArray(getClass().getClassLoader());

        mTaskTags = Lists.newArrayListWithCapacity(parcelables.length);
        for (Parcelable parcelable : parcelables) {
            mTaskTags.add((TaskTag) parcelable);
        }
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(mTag, 0);
        TaskTag[] tags = mTaskTags.toArray(new TaskTag[mTaskTags.size()]);
        parcel.writeParcelableArray(tags, 0);
    }
}
