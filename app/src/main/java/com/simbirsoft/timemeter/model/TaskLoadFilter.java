package com.simbirsoft.timemeter.model;

import com.google.common.collect.Sets;
import com.simbirsoft.timemeter.db.model.Tag;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class TaskLoadFilter {
    private final Set<Tag> mFilterTags;
    private long mDateMillis;
    private Period mPeriod;
    private String mSearchText;

    public TaskLoadFilter() {
        mFilterTags = Sets.newHashSet();
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

    public void clear() {
        mPeriod = null;
        mDateMillis = 0;
        mFilterTags.clear();
    }
}
