package com.simbirsoft.timemeter.ui.settings;

import android.os.Bundle;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.events.ImportTagsEvent;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.ImportStatsJob;
import com.simbirsoft.timemeter.jobs.SaveBackupTagsJob;
import com.simbirsoft.timemeter.jobs.SaveBackupTasksJob;
import com.simbirsoft.timemeter.persist.XmlTaskWrapper;
import com.squareup.otto.Bus;

import java.util.List;

import javax.inject.Inject;

public class ImportStatsDialog extends BackupProgressDialog implements JobLoader.JobLoaderCallbacks{

    private final static String IMPORT_BACKUP_TAG = "IMPORT_BACKUP_TAG";
    private final static String SAVE_TAGS_TAG = "SAVE_TAGS_TAG";
    private final static String SAVE_TASKS_TAG = "SAVE_TASKS_TAG";

    private List<Tag> tags = Lists.newArrayList();
    private List<Tag> actualTags = Lists.newArrayList();
    private List<XmlTaskWrapper> tasks = Lists.newArrayList();

    @Inject
    public Bus bus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Injection.sUiComponent.injectImportStatsDialog(this);

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
    public void onImportSuccess(LoadJobResult<ImportStatsJob.ImportStatsJobResult> backup) {
        this.tags = backup.getData().getTags();
        this.tasks = backup.getData().getTasks();
        requestLoad(SAVE_TAGS_TAG, this);
    }

    @OnJobFailure(ImportStatsJob.class)
    public void onImportFail() {
        displayMessage(R.string.backup_import_error);
    }

    @OnJobSuccess(SaveBackupTagsJob.class)
    public void onSaveTagsSuccess(LoadJobResult<List<Tag>> result) {
        this.actualTags = result.getData();
        requestLoad(SAVE_TASKS_TAG, this);
    }

    @OnJobFailure(SaveBackupTagsJob.class)
    public void onSaveTagsFail() {
        displayMessage(R.string.backup_import_error_tags);
    }

    @OnJobSuccess(SaveBackupTasksJob.class)
    public void onSaveTasksSuccess() {
        sendEvent();
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

    private void sendEvent() {
        final ImportTagsEvent tagsEvent = new ImportTagsEvent(actualTags);
        bus.post(tagsEvent);
    }
}