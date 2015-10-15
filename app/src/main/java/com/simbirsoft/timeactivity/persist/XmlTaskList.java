package com.simbirsoft.timeactivity.persist;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root
public class XmlTaskList {

    @Attribute(required = true)
    private int version;

    @ElementList(name = "tasks", required = false)
    private List<XmlTask> taskList;

    @ElementList(name = "tags", required = false)
    private List<XmlTag> tagList;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<XmlTask> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<XmlTask> taskList) {
        this.taskList = taskList;
    }

    public List<XmlTag> getTagList() {
        return tagList;
    }

    public void setTagList(List<XmlTag> tagList) {
        this.tagList = tagList;
    }
}
