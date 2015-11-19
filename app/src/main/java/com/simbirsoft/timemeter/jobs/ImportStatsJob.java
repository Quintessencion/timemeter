package com.simbirsoft.timemeter.jobs;

import com.be.android.library.worker.jobs.LoadJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.persist.XmlTaskList;
import com.simbirsoft.timemeter.persist.XmlTaskListReader;

import java.io.File;

import javax.inject.Inject;

public class ImportStatsJob extends LoadJob{

    @Inject
    public ImportStatsJob() {}

    @Override
    protected LoadJobResult<XmlTaskList> performLoad() throws Exception {
        final File file = new File(ExportStatsJob.STATS_PATH);
        if (!file.exists()) {
            return LoadJobResult.loadFailure();
        }

        try {
            final XmlTaskList xmlTaskList = XmlTaskListReader.readXml(file);
            return new LoadJobResult<>(xmlTaskList);
        }
        catch (Exception e) {
            return LoadJobResult.loadFailure();
        }
    }
}