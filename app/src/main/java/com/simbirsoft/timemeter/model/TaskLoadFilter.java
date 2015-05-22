package com.simbirsoft.timemeter.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.Sets;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.ui.util.TimeUtils;
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
    private long mStartDateMillis;
    private long mEndDateMillis;
    private Period mPeriod;
    private String mSearchText;
    private List<Long> mTaskIds;

    public TaskLoadFilter() {
        mFilterTags = Sets.newHashSet();
        mTaskIds = Collections.emptyList();
    }

    public TaskLoadFilter(Parcel parcel) {
        mFilterTags = Sets.newHashSet();
        mTaskIds = Collections.emptyList();
        readParcel(parcel);
    }

    public TaskLoadFilter tags(Collection<Tag> tags) {
        mFilterTags.addAll(tags);

        return this;
    }

    public TaskLoadFilter startDateMillis(long startDateMillis) {
        mStartDateMillis = startDateMillis;

        return this;
    }

    public TaskLoadFilter endDateMillis(long endDateMillis) {
        mEndDateMillis = endDateMillis;

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
                .startDateMillis(filterState.startDateMillis)
                .endDateMillis(filterState.endDateMillis)
                .period(filterState.period);

    }

    public static TaskLoadFilter fromTaskFilter(FilterView.FilterState filterState) {
        return new TaskLoadFilter()
                .startDateMillis(filterState.dateMillis)
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

    public long getStartDateMillis() {
        return mStartDateMillis;
    }

    public long getEndDateMillis() {
        return mEndDateMillis;
    }

    public Period getPeriod() {
        return mPeriod;
    }

    public List<Long> getTaskIds() { return mTaskIds; }

    public void clear() {
        mPeriod = null;
        mStartDateMillis = 0;
        mEndDateMillis = 0;
        mFilterTags.clear();
    }

    public void getDateBounds(long[] bounds) {
        bounds[0] = 0;
        bounds[1] = 0;
        if (mStartDateMillis > 0) {
            bounds[0] = TimeUtils.getDayStartMillis(mStartDateMillis);
        }
        if (mPeriod == null || mPeriod == Period.ALL) {
            return;
        }
        if (mPeriod == Period.OTHER) {
            bounds[1] = TimeUtils.getDayEndMillis(mEndDateMillis);
        } else {
            bounds[1] = Period.getPeriodEnd(mPeriod, bounds[0]);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        ArrayList<Tag> tags = new ArrayList<>();
        tags.addAll(mFilterTags);
        parcel.writeTypedList(tags);

        parcel.writeLong(mStartDateMillis);
        parcel.writeLong(mEndDateMillis);
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
        mStartDateMillis= parcel.readLong();
        mEndDateMillis = parcel.readLong();
        mPeriod = (Period)parcel.readSerializable();
        mSearchText = parcel.readString();
        mTaskIds = new ArrayList<>();
        parcel.readList(mTaskIds, Long.class.getClassLoader());
    }
}
