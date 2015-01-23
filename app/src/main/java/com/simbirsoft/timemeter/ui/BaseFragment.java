package com.simbirsoft.timemeter.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.handlers.JobEventDispatcher;
import com.be.android.library.worker.interfaces.Job;

public class BaseFragment extends Fragment {

    private JobEventDispatcher mEventDispatcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String listenerName = getClass().getName() + "event_dispatcher";
        mEventDispatcher = new JobEventDispatcher(getActivity(), listenerName);
        mEventDispatcher.register(this);
    }

    @Override
    public void onDestroy() {
        mEventDispatcher.unregister(this);
        super.onDestroy();
    }

    public void showToast(int text) {
        if (!isDetached()) {
            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
        }
    }

    protected JobEventDispatcher getJobEventDispatcher() {
        return mEventDispatcher;
    }
}
