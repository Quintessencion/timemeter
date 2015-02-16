package com.simbirsoft.timemeter.model;

import com.google.common.collect.Sets;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.jobs.LoadTaskListJob;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class TaskLoadFilter {
    private final Set<Tag> mFilterTags;
    private long mDateMillis;
    private Period mPeriod;

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

    public Set<Tag> getFilterTags() {
        return Collections.unmodifiableSet(mFilterTags);
    }

    public long getDateMillis() {
        return mDateMillis;
    }

    public Period getPeriod() {
        return mPeriod;
    }
}
