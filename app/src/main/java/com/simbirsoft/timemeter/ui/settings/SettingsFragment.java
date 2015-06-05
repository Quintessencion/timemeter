package com.simbirsoft.timemeter.ui.settings;

import android.os.Bundle;
import android.support.v4.preference.PreferenceFragment;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.main.SectionFragment;

public class SettingsFragment extends PreferenceFragment implements SectionFragment{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public int getSectionId() {
        Bundle args = getArguments();

        if (args == null || !args.containsKey(ARG_SECTION_ID)) {
            return -1;
        }

        return args.getInt(ARG_SECTION_ID);
    }

    @Override
    public String getFragmentStateKey() {
        return "_state_" + getClass().getSimpleName();
    }
}
