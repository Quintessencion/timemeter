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
import com.simbirsoft.timemeter.jobs.SaveBackupTagsJob;
import com.simbirsoft.timemeter.jobs.SaveBackupTasksJob;
import com.simbirsoft.timemeter.persist.XmlTag;
import com.simbirsoft.timemeter.persist.XmlTask;
import com.simbirsoft.timemeter.persist.XmlTaskList;

import java.util.List;

public class ImportStatsDialog extends BackupProgressDialog implements JobLoader.JobLoaderCallbacks{

    private final static String IMPORT_BACKUP_TAG = "IMPORT_BACKUP_TAG";
    private final static String SAVE_TAGS_TAG = "SAVE_TAGS_TAG";
    private final static String SAVE_TASKS_TAG = "SAVE_TASKS_TAG";

    private List<XmlTag> tags;
    private List<XmlTask> tasks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestLoad(IMPORT_BACKUP_TAG, this);
    }

    @Override
    public Job onCreateJob(String s) {
        switch (s) {
            case IMPORT_BACKUP_TAG:
                return Injection.sJobsComponent.importStatsJob();

            case SAVE_TAGS_TAG:
                SaveBackupTagsJob saveBackupTagsJob = Injection.sJobsComponent.saveBackupTagsJob();
                saveBackupTagsJob.setTags(tags);
                return saveBackupTagsJob;

            case SAVE_TASKS_TAG:
                SaveBackupTasksJob saveBackupTasksJob = Injection.sJobsComponent.saveBackupTasksJob();
                saveBackupTasksJob.setTasks(tasks);
                return saveBackupTasksJob;

            default:
                throw new IllegalArgumentException();
        }
    }

    @OnJobSuccess(ImportStatsJob.class)
    public void onImportSuccess(LoadJobResult<XmlTaskList> backup) {
        this.tags = backup.getData().getTagList();
        this.tasks = backup.getData().getTaskList();
        requestLoad(SAVE_TAGS_TAG, this);
    }

    @OnJobFailure(ImportStatsJob.class)
    public void onImportFail() {
        displayMessage(R.string.backup_import_error);
    }

    @OnJobSuccess(SaveBackupTagsJob.class)
    public void onSaveTagsSuccess() {
        requestLoad(SAVE_TASKS_TAG, this);
    }

    @OnJobFailure
    public void onSaveTagsFail() {
        displayMessage(R.string.backup_import_error_tags);
    }

    @OnJobSuccess(SaveBackupTasksJob.class)
    public void onSaveTasksSuccess() {
        displayMessage(R.string.backup_import_success);
    }

    @OnJobFailure(SaveBackupTasksJob.class)
    public void onSaveTasksFail() {
        displayMessage(R.string.backup_import_error_tasks);
    }

    @Override
    public int getTitleResId() {
        return R.string.backup_dialog_import_title;
    }
}
