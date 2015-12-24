package com.simbirsoft.timemeter.persist;

import com.simbirsoft.timemeter.db.model.TaskTimeSpan;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Date;

@Root(name = "activityEntry")
public class XmlTaskTimeSpan {

    @Attribute(required = false)
    private long id;

    @Element(required = false)
    private String description;

    @Attribute(required = true)
    private Date startDate;

    @Attribute(required = true)
    private Date endDate;

    public XmlTaskTimeSpan() {}

    public XmlTaskTimeSpan(TaskTimeSpan taskTimeSpan) {
        id = taskTimeSpan.getId();
        description = taskTimeSpan.getDescription();
        startDate = new Date(taskTimeSpan.getStartTimeMillis());
        endDate = new Date(taskTimeSpan.getEndTimeMillis());
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
