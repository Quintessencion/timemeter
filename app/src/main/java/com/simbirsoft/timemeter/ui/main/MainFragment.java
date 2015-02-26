package com.simbirsoft.timemeter.ui.main;

import android.os.Bundle;

import com.simbirsoft.timemeter.ui.base.BaseFragment;

public class MainFragment extends BaseFragment {

    public static final String ARG_SECTION_ID = "MainFragment_section_id";

    public int getSectionId() {
        Bundle args = getArguments();

        if (args == null || !args.containsKey(ARG_SECTION_ID)) {
            return -1;
        }

        return args.getInt(ARG_SECTION_ID);
    }

    public String getPageTitle() {
        return getClass().getSimpleName();
    }

    public String getFragmentStateKey() {
        return "_state_" + getClass().getSimpleName();
    }
}
