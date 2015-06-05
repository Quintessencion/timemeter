package com.simbirsoft.timemeter.ui.settings;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.jobs.LoadTagListJob;
import com.simbirsoft.timemeter.ui.main.MainFragment;

import org.androidannotations.annotations.EFragment;

import java.util.List;

@EFragment(R.layout.fragment_settings)
public class SettingsFragment extends MainFragment implements JobLoader.JobLoaderCallbacks {

    // TODO: impl job for loading settings
    @Override
    public Job onCreateJob(String s) {
        return null;
    }


    // TODO: impl job for loading settings
    @OnJobSuccess(LoadTagListJob.class)
    public void onSettingsLoaded(LoadJobResult<List<Tag>> result) {

    }

    // TODO: impl job for loading settings
    @OnJobFailure(LoadTagListJob.class)
    public void onSettingsLoadFailed() {

    }
}
