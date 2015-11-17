package com.simbirsoft.timemeter.ui.settings;

public interface ExportStatsCallback {

    public void onTaskLoaded();
    public void onTagLoaded();
    public void onSuccess();
    public void onError(String error);
}
