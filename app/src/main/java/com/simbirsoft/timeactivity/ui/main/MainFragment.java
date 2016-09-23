package com.simbirsoft.timeactivity.ui.main;

import android.os.Bundle;

import com.simbirsoft.timeactivity.ui.base.BaseFragment;

public class MainFragment extends BaseFragment implements SectionFragment {
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
