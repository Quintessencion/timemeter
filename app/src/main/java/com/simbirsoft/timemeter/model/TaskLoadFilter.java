package com.simbirsoft.timemeter.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.Sets;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.ui.views.FilterView;
import com.simbirsoft.timemeter.ui.views.TaskActivitiesFilterView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TaskLoadFilter implements Parcelable {
    public static final Parcelable.Creator<TaskLoadFilter> CREATOR =
            new Parcelable.Creator<TaskLoadFilter>() {
                @Override
                public TaskLoadFilter createFromParcel(Parcel parcel) {
                    return new TaskLoadFilter(parcel);
                }

                @Override
                public TaskLoadFilter[] newArray(int sz) {
                    return new TaskLoadFilter[sz];
                }
            };

    private final Set<Tag> mFilterTags;
    private long mDateMillis;
    private Period mPeriod;
    private String mSearchText;
    private List<Long> mTaskIds;

    public TaskLoadFilter() {
        mFilterTags = Sets.newHashSet();
    }

    public TaskLoadFilter(Parcel parcel) {
        mFilterTags = Sets.newHashSet();
        readParcel(parcel);
    }

    public TaskLoadFilter tags(Collection<Tag> tags) {
        mFilterTags.addAll(tags);

        return this;
    }

    public TaskLoadFilter dateMillis(long dateMillis) {
        mDateMillis = dateMillis;

        return this;
    }

    public TaskLoadFilter period(Period period) {
        mPeriod = period;

        return this;
    }

    public TaskLoadFilter searchText(String searchText) {
        mSearchText = searchText;

        return this;
    }

    public TaskLoadFilter taskIds(List<Long> taskIds) {
        mTaskIds = taskIds;

        return this;
    }

    public static TaskLoadFilter fromTaskActivitiesFilter(TaskActivitiesFilterView.FilterState filterState) {
        return new TaskLoadFilter()
                .dateMillis(filterState.startDateMillis)
                .period(filterState.period);

    }

    public static TaskLoadFilter fromTaskFilter(FilterView.FilterState filterState) {
        return new TaskLoadFilter()
                .dateMillis(filterState.dateMillis)
                .period(filterState.period)
                .tags(filterState.tags)
                .searchText(filterState.searchText);
    }

    public String getSearchText() {
        return mSearchText;
    }

    public Set<Tag> getFilterTags() {
        return Collections.unmodifiableSet(mFilterTags);
    }

    public long getDateMillis() {
        return mDateMillis;
    }

    public Period getPeriod() {
        return mPeriod;
    }

    public List<Long> getTaskIds() { return mTaskIds; }

    public void clear() {
        mPeriod = null;
        mDateMillis = 0;
        mFilterTags.clear();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        ArrayList<Tag> tags = new ArrayList<>();
        tags.addAll(mFilterTags);
        parcel.writeList(tags);

        parcel.writeLong(mDateMillis);
        parcel.writeSerializable(mPeriod);
        parcel.writeString(mSearchText);

        ArrayList<Long> taskIds = new ArrayList<>();
        taskIds.addAll(mTaskIds);
        parcel.writeList(taskIds);
    }

    private void readParcel(Parcel parcel) {
        ArrayList<Tag> tags = new ArrayList<>();
        parcel.readTypedList(tags, Tag.CREATOR);
        mFilterTags.addAll(tags);
        mDateMillis = parcel.readLong();
        mPeriod = (Period)parcel.readSerializable();
        mSearchText = parcel.readString();
        mTaskIds = new ArrayList<>();
        parcel.readList(mTaskIds, Long.class.getClassLoader());
    }
}
