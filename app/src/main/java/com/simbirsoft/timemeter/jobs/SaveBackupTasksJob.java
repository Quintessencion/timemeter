package com.simbirsoft.timemeter.jobs;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.persist.XmlTaskWrapper;
import com.simbirsoft.timemeter.ui.model.TaskBundle;

import java.util.List;

import javax.inject.Inject;

public class SaveBackupTasksJob extends LoadJob{

    private List<XmlTaskWrapper> backupTasks;

    @Inject
    public SaveBackupTasksJob() {}

    @Override
    protected LoadJobResult<?> performLoad() throws Exception {
        SaveTaskBundleJob saveTaskBundleJob = Injection.sJobsComponent.saveTaskBundleJob();
        LoadTagListJob loadTagListJob = Injection.sJobsComponent.loadTagListJob();

        List<TaskBundle> result = Lists.newArrayList();

        for (XmlTaskWrapper xmlTaskWrapper: backupTasks) {
            final List<Tag> tags = loadTagListJob.getTagListWereIds(xmlTaskWrapper.getTags());

            TaskBundle taskBundle = new TaskBundle();
            taskBundle.setTask(xmlTaskWrapper.getTask());
            taskBundle.setTags(tags);
            taskBundle.setTaskTimeSpans(xmlTaskWrapper.getSpans());

            result.add(taskBundle);

            saveTaskBundleJob.saveTaskBundle(taskBundle);
        }

        return new LoadJobResult<>(result);
    }

    public void setTasks(List<XmlTaskWrapper> backupTasks) {
        this.backupTasks = backupTasks;
    }
}