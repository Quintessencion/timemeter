package com.simbirsoft.timeactivity.ui.util;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.simbirsoft.timeactivity.db.model.Task;
import com.simbirsoft.timeactivity.model.Period;
import com.simbirsoft.timeactivity.ui.model.TaskBundle;
import com.simbirsoft.timeactivity.ui.views.FilterView;

/**
 * Test task bundle for specified task filter
 *
 * Only task creation time and tags are used to filter
 */
public class TaskFilterPredicate implements Predicate<TaskBundle> {

    private FilterView.FilterState mTaskFilterState;

    public TaskFilterPredicate(FilterView.FilterState taskFilterState) {
        mTaskFilterState = taskFilterState;
    }

    @Override
    public boolean apply(TaskBundle input) {
        if (mTaskFilterState.tags != null &&
            !mTaskFilterState.tags.isEmpty()) {

            if (input.getTags() == null ||
                input.getTags().isEmpty()) {
                return false;
            }

            if (!mTaskFilterState.tags.containsAll(input.getTags())) {
                return false;
            }
        }

        if (!Strings.isNullOrEmpty(mTaskFilterState.searchText)) {
            final String description = input.getTask() != null
                    ? input.getTask().getDescription()
                    : "";

            if (!description.toUpperCase().contains(mTaskFilterState.searchText.toUpperCase())) {
                return false;
            }
        }

        if (mTaskFilterState.dateMillis != 0) {
            long periodEnd = 0;
            if (mTaskFilterState.period != null) {
                periodEnd = Period.getPeriodEnd(mTaskFilterState.period,
                                                mTaskFilterState.dateMillis);
            }

            final Task task = input.getTask();
            long taskCreateTime = task.getCreateDate().getTime();
            if (taskCreateTime < mTaskFilterState.dateMillis) {
                return false;
            }
            if (periodEnd > 0 && taskCreateTime >= periodEnd) {
                return false;
            }
        }

        return true;
    }
}
