package com.simbirsoft.timemeter.ui.settings;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.ExportStatsJob;
import com.simbirsoft.timemeter.jobs.LoadTagListJob;
import com.simbirsoft.timemeter.jobs.LoadTaskListJob;
import com.simbirsoft.timemeter.ui.model.TaskBundle;

import java.util.List;

public class ExportStats implements JobLoader.JobLoaderCallbacks{

    public static final String TASK_LIST_LOADER_TAG = "SettingsFragment_taskLoaderTag";
    public static final String TAGS_LIST_LOADER_TAG = "SettingsFragment_tagsLoaderTag";
    public static final String BACKUP_SAVE_TAG = "SettingsFragment_backupSaveTag";

    private static final int LOAD_TASK_JOB_ID = 2970017;

    private List<TaskBundle> tasks = Lists.newArrayList();
    private List<Tag> tags = Lists.newArrayList();

    private final Resources resources;
    private final ExportStatsCallback exportStatsCallback;

    public ExportStats(@NonNull final ExportStatsCallback exportStatsCallback) {
        this.exportStatsCallback = exportStatsCallback;
        this.resources = ((Fragment) exportStatsCallback).getActivity().getResources();
    }

    @Override
    public Job onCreateJob(String s) {
        switch (s) {
            case TAGS_LIST_LOADER_TAG:
                return Injection.sJobsComponent.loadTagListJob();

            case TASK_LIST_LOADER_TAG:
                LoadTaskListJob job = Injection.sJobsComponent.loadTaskListJob();
                job.setGroupId(LOAD_TASK_JOB_ID);
                job.addTag(s);
                return job;

            case BACKUP_SAVE_TAG:
                ExportStatsJob exportStatsJob = Injection.sJobsComponent.exportStatsJob();
                exportStatsJob.setTasks(tasks);
                exportStatsJob.setTags(tags);
                return exportStatsJob;

            default:
                throw new IllegalArgumentException();
        }
    }

    @OnJobSuccess(LoadTaskListJob.class)
    public void onTaskListLoaded(LoadJobResult<List<TaskBundle>> event) {
        tasks = event.getData();
        exportStatsCallback.onTaskLoaded();
    }

    @OnJobFailure(LoadTaskListJob.class)
    public void onTaskListLoadFailed() {
        sendError(R.string.backup_export_error_task_load);
    }

    @OnJobSuccess(LoadTagListJob.class)
    public void onTagListLoaded(LoadJobResult<List<Tag>> event) {
        tags = event.getData();
        exportStatsCallback.onTagLoaded();
    }

    @OnJobFailure(LoadTagListJob.class)
    public void onTagListLoadFailed() {
        sendError(R.string.backup_export_error_tag_load);
    }

    @OnJobSuccess(ExportStatsJob.class)
    public void onBackupSuccess() {
        exportStatsCallback.onSuccess();
    }

    @OnJobFailure(ExportStatsJob.class)
    public void onBackupFail() {
        sendError(R.string.backup_export_error_write);
    }

    private void sendError(int resId) {
        final String error = resources.getString(resId);
        exportStatsCallback.onError(error);
    }
}