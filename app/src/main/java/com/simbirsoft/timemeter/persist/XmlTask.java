package com.simbirsoft.timemeter.persist;

import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.Date;
import java.util.List;

@Root(name = "task")
public class XmlTask {

    @Attribute(required = false)
    private long id;

    @Element(required = true)
    private String description;

    @Attribute(required = false)
    private Date createDate;

    @ElementList(name = "tagReferences", required = false)
    private List<XmlTagRef> tagList;

    @ElementList(name = "activity", required = false)
    private List<XmlTaskTimeSpan> timeSpanList;

    public XmlTask(Task task) {
        id = task.getId();
        description = task.getDescription();
        createDate = task.getCreateDate();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public List<XmlTagRef> getTagList() {
        return tagList;
    }

    public void setTagList(List<XmlTagRef> tagList) {
        this.tagList = tagList;
    }

    public List<XmlTaskTimeSpan> getTimeSpanList() {
        return timeSpanList;
    }

    public void setTimeSpanList(List<XmlTaskTimeSpan> timeSpanList) {
        this.timeSpanList = timeSpanList;
    }

    public Task getTask() {
        Task task = new Task();

        if (id > 0) {
            task.setId(id);
        }
        task.setDescription(description);

        if (createDate == null) {
            task.setCreateDate(new Date(System.currentTimeMillis()));
        } else {
            task.setCreateDate(createDate);
        }

        return task;
    }

    public List<TaskTimeSpan> getTaskActivity() {
        final List<TaskTimeSpan> result = Lists.newArrayListWithCapacity(timeSpanList.size());

        for (XmlTaskTimeSpan span : timeSpanList) {
            TaskTimeSpan taskSpan = new TaskTimeSpan();
            if (id > 0) {
                taskSpan.setTaskId(id);
            }
            taskSpan.setDescription(span.getDescription());
            taskSpan.setStartTimeMillis(span.getStartDate().getTime());
            taskSpan.setEndTimeMillis(span.getEndDate().getTime());

            result.add(taskSpan);
        }

        return result;
    }
}
