package com.simbirsoft.timemeter.ui.base;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.be.android.library.worker.handlers.JobEventDispatcher;
import com.simbirsoft.timemeter.log.LogFactory;

import org.androidannotations.annotations.EFragment;
import org.slf4j.Logger;

@EFragment
public class BaseDialogFragment extends DialogFragment implements FragmentContainerCallbacks {
    private static final Logger LOG = LogFactory.getLogger(BaseDialogFragment.class);

    private FragmentContainerCallbacks mCallbacks;
    private JobEventDispatcher mJobEventDispatcher;
    private boolean isAttached;

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

    protected JobEventDispatcher getJobEventHandler() {
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
}
