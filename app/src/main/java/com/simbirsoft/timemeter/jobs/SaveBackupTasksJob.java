package com.simbirsoft.timemeter.jobs;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.persist.XmlTagListConverter;
import com.simbirsoft.timemeter.persist.XmlTask;
import com.simbirsoft.timemeter.ui.model.TaskBundle;

import java.util.List;

import javax.inject.Inject;

public class SaveBackupTasksJob extends LoadJob{

    private List<XmlTask> backupTasks;

    @Inject
    public SaveBackupTasksJob() {}

    @Override
    protected LoadJobResult<?> performLoad() throws Exception {
        SaveTaskBundleJob saveTaskBundleJob = Injection.sJobsComponent.saveTaskBundleJob();
        LoadTagListJob loadTagListJob = Injection.sJobsComponent.loadTagListJob();

        for (XmlTask xmlTask: backupTasks) {
            final List<Long> ids = XmlTagListConverter.toIdsList(xmlTask.getTagList());
            final List<Tag> tags = loadTagListJob.getTagListWereIds(ids);

            TaskBundle taskBundle = new TaskBundle();
            taskBundle.setTask(xmlTask.getTask());
            taskBundle.setTags(tags);
            taskBundle.setTaskTimeSpans(xmlTask.getTaskActivity());

            saveTaskBundleJob.saveTaskBundle(taskBundle);
        }

        return LoadJobResult.loadOk();
    }

    public void setTasks(List<XmlTask> backupTasks) {
        this.backupTasks = backupTasks;
    }
}