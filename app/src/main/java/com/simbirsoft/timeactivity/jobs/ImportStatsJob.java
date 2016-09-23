package com.simbirsoft.timeactivity.jobs;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.simbirsoft.timeactivity.db.model.Tag;
import com.simbirsoft.timeactivity.persist.XmlTag;
import com.simbirsoft.timeactivity.persist.XmlTagRef;
import com.simbirsoft.timeactivity.persist.XmlTaskList;
import com.simbirsoft.timeactivity.persist.XmlTaskListReader;
import com.simbirsoft.timeactivity.persist.XmlTaskWrapper;

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
            final List<XmlTaskWrapper> tasks = Lists.newArrayList(Collections2.transform(xmlTaskList.getTaskList(), input -> new XmlTaskWrapper(input.getTask(), transformToTagsId(input.getTagList()), input.getTaskActivity())));

            final ImportStatsJobResult result = new ImportStatsJobResult(tags, tasks);

            return new LoadJobResult<>(result);
        }
        catch (Exception e) {
            return LoadJobResult.loadFailure();
        }
    }

    public static class ImportStatsJobResult {
        private List<Tag> tags = Lists.newArrayList();
        private List<XmlTaskWrapper> tasks = Lists.newArrayList();

        public ImportStatsJobResult(List<Tag> tags, List<XmlTaskWrapper> tasks) {
            this.tags = tags;
            this.tasks = tasks;
        }

        public List<Tag> getTags() {
            return tags;
        }

        public List<XmlTaskWrapper> getTasks() {
            return tasks;
        }
    }

    private List<Long> transformToTagsId(List<XmlTagRef> ids) {
        return Lists.newArrayList(Collections2.transform(ids, XmlTagRef::getTagId));
    }
}