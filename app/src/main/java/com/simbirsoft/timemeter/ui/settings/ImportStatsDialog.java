package com.simbirsoft.timemeter.ui.settings;

import android.os.Bundle;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.ImportStatsJob;
import com.simbirsoft.timemeter.persist.XmlTaskList;

public class ImportStatsDialog extends BackupProgressDialog implements JobLoader.JobLoaderCallbacks{

    private final String IMPORT_BACKUP_TAG = "IMPORT_BACKUP_TAG";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestLoad(IMPORT_BACKUP_TAG, this);
    }

    @Override
    public Job onCreateJob(String s) {
        if (s.equals(IMPORT_BACKUP_TAG)) {
            return Injection.sJobsComponent.importStatsJob();
        }

        throw new IllegalArgumentException();
    }

    @OnJobSuccess(ImportStatsJob.class)
    public void onImportSuccess(LoadJobResult<XmlTaskList> backup) {
        showToast(R.string.backup_import_success);
        this.dismiss();
    }

    @OnJobFailure(ImportStatsJob.class)
    public void onImportFail() {
        showToast(R.string.backup_import_error);
        this.dismiss();
    }

    @Override
    public int getTitleResId() {
        return R.string.backup_dialog_import_title;
    }
}
