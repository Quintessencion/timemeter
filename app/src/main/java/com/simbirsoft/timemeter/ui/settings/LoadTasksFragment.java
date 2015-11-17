package com.simbirsoft.timemeter.ui.settings;

import android.os.Bundle;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.base.BaseFragment;

import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_load_tasks)
public class LoadTasksFragment extends BaseFragment implements ExportStatsCallback {

    private final ExportStats exportStats = new ExportStats(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestLoad(ExportStats.TASK_LIST_LOADER_TAG, exportStats);
    }

    @Override
    public void onTaskLoaded() {
        requestLoad(ExportStats.TAGS_LIST_LOADER_TAG, exportStats);
    }

    @Override
    public void onTagLoaded() {
        requestLoad(ExportStats.BACKUP_SAVE_TAG, exportStats);
    }

    @Override
    public void onSuccess() {
        showToast(R.string.backup_export_success);
    }

    @Override
    public void onError(String error) {
        showToast(error);
    }
}