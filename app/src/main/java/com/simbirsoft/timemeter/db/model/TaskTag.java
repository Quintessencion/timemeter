package com.simbirsoft.timemeter.db.model;

public class TaskTag {

    private Long _id;
    private Long taskId;
    private Long tagId;

    public Long getId() {
        return _id;
    }

    public void setId(Long id) {
        this._id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    @Override
    public String toString() {
        return "TaskTag{" +
                "_id=" + _id +
                ", taskId=" + taskId +
                ", tagId=" + tagId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskTag taskTag = (TaskTag) o;

        if (_id != null ? !_id.equals(taskTag._id) : taskTag._id != null) return false;
        if (tagId != null ? !tagId.equals(taskTag.tagId) : taskTag.tagId != null) return false;
        if (taskId != null ? !taskId.equals(taskTag.taskId) : taskTag.taskId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _id != null ? _id.hashCode() : 0;
        result = 31 * result + (taskId != null ? taskId.hashCode() : 0);
        result = 31 * result + (tagId != null ? tagId.hashCode() : 0);
        return result;
    }
}
