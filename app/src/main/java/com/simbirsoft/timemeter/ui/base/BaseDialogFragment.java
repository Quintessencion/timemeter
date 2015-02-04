package com.simbirsoft.timemeter.ui.base;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobLoaderManager;
import com.be.android.library.worker.handlers.JobEventDispatcher;
import com.be.android.library.worker.interfaces.Job;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.log.LogFactory;

import org.androidannotations.annotations.EFragment;
import org.slf4j.Logger;

import java.util.LinkedList;

@EFragment
public class BaseDialogFragment extends DialogFragment implements FragmentContainerCallbacks {
    private static final Logger LOG = LogFactory.getLogger(BaseDialogFragment.class);

    private FragmentContainerCallbacks mCallbacks;
    private JobEventDispatcher mJobEventDispatcher;
    private boolean isAttached;
    private boolean mShouldSubscribeForJobEvents = true;
    private boolean mIsSubscribedForJobEvents;
    private LinkedList<Bundle> mPendingAlerts;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        isAttached = true;

        try {
            mCallbacks = (FragmentContainerCallbacks) activity;

        } catch (ClassCastException e) {
            LOG.error("containing activity should implement {}",
                    FragmentContainerCallbacks.class.getName());

            throw e;
        }
    }

    @Override
    public void onDetach() {
        isAttached = false;

        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mJobEventDispatcher = new JobEventDispatcher(getActivity());
        if (shouldSubscribeForJobEvents()) {
            mJobEventDispatcher.register(this);
            setSubscribedForJobEvents(true);
        }
    }

    @Override
    public void onDestroy() {
        if (isSubscribedForJobEvents()) {
            mJobEventDispatcher.unregister(this);
        }
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mJobEventDispatcher.restoreState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mJobEventDispatcher.saveState(outState);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }

        super.onDestroyView();
    }

    public boolean isAttached() {
        return isAttached;
    }

    protected JobEventDispatcher getJobEventDispatcher() {
        return mJobEventDispatcher;
    }

    @Override
    public void finish() {
        mCallbacks.finish();
    }

    @Override
    public void setResult(int resultCode, Intent data) {
        mCallbacks.setResult(resultCode, data);
    }

    @Override
    public void hideToolbar() {
    }

    @Override
    public void showToolbar() {
    }

    @Override
    public Toolbar getToolbar() {
        return null;
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

    protected int submitJob(Job job) {
        return mJobEventDispatcher.submitJob(job);
    }

    protected int requestLoad(String loaderAttachTag, JobLoader.JobLoaderCallbacks callbacks) {
        JobLoaderManager mgr = JobLoaderManager.getInstance();
        JobLoader loader = mgr.initLoader(mJobEventDispatcher, loaderAttachTag, callbacks);

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
}
