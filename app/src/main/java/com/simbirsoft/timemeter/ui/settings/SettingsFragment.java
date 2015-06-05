package com.simbirsoft.timemeter.ui.settings;

import android.os.Bundle;
import android.support.v4.preference.PreferenceFragment;

import com.simbirsoft.timemeter.R;

public class SettingsFragment extends PreferenceFragment {

  @Override
  public void onCreate(Bundle savedInstanceState) {
   super.onCreate(savedInstanceState);

   // Load the preferences from an XML resource
   addPreferencesFromResource(R.xml.preferences);
  }

   /* // TODO: impl job for loading settings
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

    } */
}
