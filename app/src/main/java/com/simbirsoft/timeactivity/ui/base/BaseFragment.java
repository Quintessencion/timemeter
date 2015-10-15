package com.simbirsoft.timeactivity.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobLoaderManager;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.handlers.JobEventDispatcher;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.util.JobSelector;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.ui.util.ToastUtils;

import java.util.LinkedList;

public class BaseFragment extends Fragment {

    private JobEventDispatcher mEventDispatcher;
    private boolean mShouldSubscribeForJobEvents = true;
    private boolean mIsSubscribedForJobEvents;
    private LinkedList<Bundle> mPendingAlerts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String listenerName = getClass().getName() + "event_dispatcher";
        mEventDispatcher = new JobEventDispatcher(getActivity(), listenerName);
        mEventDispatcher.restoreState(savedInstanceState);
        if (shouldSubscribeForJobEvents()) {
            mEventDispatcher.register(this);
            setSubscribedForJobEvents(true);
        }
    }

    @Override
    public void onDestroy() {
        if (isSubscribedForJobEvents()) {
            mEventDispatcher.unregister(this);
        }
        super.onDestroy();
    }

    public boolean handleBackPress() {
        return false;
    }

    protected JobEventDispatcher getJobEventDispatcher() {
        return mEventDispatcher;
    }

    public boolean shouldSubscribeForJobEvents() {
        return mShouldSubscribeForJobEvents;
    }

    public void setShouldSubscribeForJobEvents(boolean shouldSubscribeForJobEvents) {
        mShouldSubscribeForJobEvents = shouldSubscribeForJobEvents;
    }

    protected boolean isSubscribedForJobEvents() {
        return mIsSubscribedForJobEvents;
    }

    private void setSubscribedForJobEvents(boolean isSubscribedForJobEvents) {
        mIsSubscribedForJobEvents = isSubscribedForJobEvents;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mEventDispatcher.saveState(outState);
    }

    protected void showToast(int stringId) {
        if (!isVisible()) return;

        showToast(stringId, Toast.LENGTH_SHORT);
    }

    protected void showToast(String text) {
        if (!isVisible()) return;

        showToast(text, Toast.LENGTH_SHORT);
    }

    protected void showToast(int stringId, int toastLength) {
        if (!isVisible()) return;

        Toast.makeText(getActivity(), stringId, toastLength).show();
    }

    protected void showToast(String text, int toastLength) {
        if (!isVisible()) return;

        Toast.makeText(getActivity(), text, toastLength).show();
    }

    protected void showToastWithAnchor(int toastText, View anchor) {
        if (!isVisible()) return;

        ToastUtils.showToastWithAnchor(getActivity(),
                getString(toastText), anchor, Toast.LENGTH_SHORT);
    }

    protected void showToastWithAnchor(String toastText, View anchor) {
        if (!isVisible()) return;

        ToastUtils.showToastWithAnchor(getActivity(),
                toastText, anchor, Toast.LENGTH_SHORT);
    }

    protected int submitJob(Job job) {
        return mEventDispatcher.submitJob(job);
    }

    protected int requestLoad(String loaderAttachTag, JobLoader.JobLoaderCallbacks callbacks) {
        JobLoaderManager mgr = JobLoaderManager.getInstance();
        JobLoader loader = mgr.initLoader(mEventDispatcher, loaderAttachTag, callbacks);

        return loader.requestLoad();
    }
    protected int requestReload(String loaderAttachTag, JobLoader.JobLoaderCallbacks callbacks) {
        JobManager.getInstance().cancelAll(JobSelector.forJobTags(loaderAttachTag));

        JobLoaderManager mgr = JobLoaderManager.getInstance();
        JobLoader loader = mgr.initLoader(mEventDispatcher, loaderAttachTag, callbacks);

        return loader.requestLoad();
    }

    protected void displayAlert(int titleResId, int messageResId) {
        final String title = getString(titleResId);
        final String message = getString(messageResId);

        displayAlert(title, message);
    }

    protected void displayAlert(String title, String message) {
        final String acceptCaption = getString(R.string.action_accept);

        Bundle args = AppAlertDialogFragment.prepareArgs(title, message, acceptCaption);

        if (!isVisible()) {
            if (mPendingAlerts == null) {
                mPendingAlerts = new LinkedList<>();
            }
            mPendingAlerts.addLast(args);

        } else {
            displayAlertDialogImpl(args);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (!isVisibleToUser) {
            return;
        }

        if (mPendingAlerts != null) {
            for (Bundle args : mPendingAlerts) {
                displayAlertDialogImpl(args);
            }
        }
    }

    protected void displayAlertDialogImpl(Bundle alertDialogArgs) {
        Intent launchIntent = DialogContainerActivity.prepareDialogLaunchIntent(
                getActivity(),
                AppAlertDialogFragment.class.getName(),
                alertDialogArgs);

        startActivity(launchIntent);
    }

    protected void showSnackBarLightRed(int titleResId) {
        Snackbar bar = Snackbar.with(getActivity())
                .type(SnackbarType.MULTI_LINE)
                .text(titleResId)
                .colorResource(R.color.lightRed)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE);
        SnackbarManager.show(bar);
    }

    protected void showSnackBarLightRed(String titleStr) {
        Snackbar bar = Snackbar.with(getActivity())
                .type(SnackbarType.MULTI_LINE)
                .text(titleStr)
                .colorResource(R.color.lightRed)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE);
        SnackbarManager.show(bar);
    }

}
