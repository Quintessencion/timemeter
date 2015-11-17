package com.simbirsoft.timemeter.jobs;

import android.os.Environment;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.persist.XmlCreatorUtil;
import com.simbirsoft.timemeter.persist.XmlTaskList;
import com.simbirsoft.timemeter.ui.model.TaskBundle;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

public class ExportStatsJob extends LoadJob{

    private List<TaskBundle> tasks;
    private List<Tag> tags;

    @Inject
    public ExportStatsJob() {}

    @Override
    protected LoadJobResult<?> performLoad() throws Exception {
        XmlTaskList xmlTaskList = XmlCreatorUtil.taskBundleToXmlTaskList(tasks);
        xmlTaskList.setTagList(XmlCreatorUtil.tagsToXmlTags(tags));

        Serializer serializer = new Persister();

        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "stats.xml";

        File file = new File(sdPath);

        try {
            serializer.write(xmlTaskList, file);
            return LoadJobResult.loadOk();
        }
        catch (Exception e) {
            return LoadJobResult.loadFailure();
        }
    }

    public void setTasks(List<TaskBundle> tasks) {
        this.tasks = tasks;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}