package com.simbirsoft.timemeter.jobs;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.persist.XmlTag;
import com.simbirsoft.timemeter.persist.XmlTask;
import com.simbirsoft.timemeter.persist.XmlTaskList;
import com.simbirsoft.timemeter.persist.XmlTaskListReader;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

public class ImportStatsJob extends LoadJob{

    @Inject
    public ImportStatsJob() {}

    @Override
    protected LoadJobResult<ImportStatsJobResult> performLoad() throws Exception {
        final File file = new File(ExportStatsJob.STATS_PATH);
        if (!file.exists()) {
            return LoadJobResult.loadFailure();
        }

        try {
            final XmlTaskList xmlTaskList = XmlTaskListReader.readXml(file);

            final List<Tag> tags = Lists.newArrayList(Collections2.transform(xmlTaskList.getTagList(), XmlTag::getTag));
            final List<Task> tasks = Lists.newArrayList(Collections2.transform(xmlTaskList.getTaskList(), XmlTask::getTask));

            final ImportStatsJobResult result = new ImportStatsJobResult(tags, tasks);

            return new LoadJobResult<>(result);
        }
        catch (Exception e) {
            return LoadJobResult.loadFailure();
        }
    }

    public static class ImportStatsJobResult {
        private List<Tag> tags = Lists.newArrayList();
        private List<Task> tasks = Lists.newArrayList();

        public ImportStatsJobResult(List<Tag> tags, List<Task> tasks) {
            this.tags = tags;
            this.tasks = tasks;
        }

        public List<Tag> getTags() {
            return tags;
        }

        public List<Task> getTasks() {
            return tasks;
        }
    }
}