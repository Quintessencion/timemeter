package com.simbirsoft.timemeter.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobLoaderManager;
import com.be.android.library.worker.handlers.JobEventDispatcher;
import com.be.android.library.worker.interfaces.Job;

public class BaseActivity extends ActionBarActivity {

    private JobEventDispatcher mJobEventDispatcher;
    private boolean isStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mJobEventDispatcher = new JobEventDispatcher(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        isStarted = true;
    }

    @Override
    protected void onStop() {
        isStarted = false;

        super.onStop();
    }

    public boolean isStarted() {
        return isStarted;
    }

    protected JobEventDispatcher getJobEventHandler() {
        return mJobEventDispatcher;
    }

    protected void showToast(int stringId) {
        showToast(stringId, Toast.LENGTH_SHORT);
    }

    protected void showToast(int stringId, int toastLength) {
        Toast.makeText(this, stringId, toastLength).show();
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mJobEventDispatcher.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mJobEventDispatcher.restoreState(savedInstanceState);
    }

    protected int submitJob(Job job) {
        return mJobEventDispatcher.submitJob(job);
    }

    protected void registerForJobEvents() {
        mJobEventDispatcher.register(this);
    }

    protected void unregisterForJobEvents() {
        mJobEventDispatcher.unregister(this);
    }

    protected int requestLoad(String loaderAttachTag, JobLoader.JobLoaderCallbacks callbacks) {
        JobLoaderManager mgr = JobLoaderManager.getInstance();
        JobLoader loader = mgr.initLoader(mJobEventDispatcher, loaderAttachTag, callbacks);

        return loader.requestLoad();
    }

}
