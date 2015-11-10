package com.simbirsoft.timemeter.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobLoaderManager;
import com.be.android.library.worker.handlers.JobEventDispatcher;
import com.be.android.library.worker.interfaces.Job;
import com.simbirsoft.timemeter.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;

@EActivity
public class BaseActivity extends AppCompatActivity {

    @SystemService
    public InputMethodManager inputMethodManager;

    private JobEventDispatcher mJobEventDispatcher;
    private boolean isStarted;
    private Toolbar mToolbar;

    @AfterViews
    public void hideKeyboard() {
        View view = mainView();
        hideKeyboard(view);
    }

    public @Nullable Toolbar getToolbar() {
        if (mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
        }

        return mToolbar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mJobEventDispatcher = new JobEventDispatcher(this);
        mJobEventDispatcher.restoreState(savedInstanceState);
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

    protected View mainView() {
        return null;
    }

    private void hideKeyboard(View view) {
        if (view == null) {
            return;
        }

        if(!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                if (BaseActivity.this.getCurrentFocus() != null) {
                    inputMethodManager.hideSoftInputFromWindow(BaseActivity.this.getCurrentFocus().getWindowToken(), 0);
                }
                return false;
            });
        }

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                hideKeyboard(innerView);
            }
        }
    }
}