package com.simbirsoft.timemeter.persist;

import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.ui.model.TaskBundle;

import java.util.ArrayList;
import java.util.List;

public class XmlCreatorUtil {
    public static XmlTaskList taskBundleToXmlTaskList(List<TaskBundle> taskBundles) {
        XmlTaskList xmlTaskList = new XmlTaskList();

        List<XmlTask> xmlTasks = new ArrayList<>();

        for(TaskBundle taskBundle: taskBundles) {
            XmlTask xmlTask = new XmlTask(taskBundle.getTask());
            xmlTask.setTagList(tagsToTagRef(taskBundle.getTags()));
            xmlTask.setTimeSpanList(timeSpansToXmlTaskTimeSpans(taskBundle.getTaskTimeSpans()));
            xmlTasks.add(xmlTask);
        }

        xmlTaskList.setTaskList(xmlTasks);
        return xmlTaskList;
    }

    public static List<XmlTagRef> tagsToTagRef(List<Tag> tags) {
        List<XmlTagRef> xmlTagRefs = new ArrayList<>();

        for (Tag tag: tags) {
            xmlTagRefs.add(new XmlTagRef(tag));
        }

        return xmlTagRefs;
    }

    public static List<XmlTaskTimeSpan> timeSpansToXmlTaskTimeSpans(List<TaskTimeSpan> timeSpans) {
        List<XmlTaskTimeSpan> xmlTaskTimeSpans = new ArrayList<>();

        for (TaskTimeSpan taskTimeSpan: timeSpans) {
            xmlTaskTimeSpans.add(new XmlTaskTimeSpan(taskTimeSpan));
        }

        return xmlTaskTimeSpans;
    }

    public static List<XmlTag> tagsToXmlTags(List<Tag> tags) {
        List<XmlTag> xmlTags = new ArrayList<>();

        for (Tag tag: tags) {
            xmlTags.add(new XmlTag(tag));
        }

        return xmlTags;
    }
}
