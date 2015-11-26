package com.simbirsoft.timemeter.persist;

import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;

import java.util.List;

public class XmlTaskWrapper {

    private List<Long> tags;
    private List<TaskTimeSpan> spans;
    private Task task;

    public XmlTaskWrapper(Task task, List<Long> tags, List<TaskTimeSpan> spans) {
        this.task = task;
        this.tags = tags;
        this.spans = spans;
    }

    public List<Long> getTags() {
        return tags;
    }

    public List<TaskTimeSpan> getSpans() {
        return spans;
    }

    public Task getTask() {
        return task;
    }
}
