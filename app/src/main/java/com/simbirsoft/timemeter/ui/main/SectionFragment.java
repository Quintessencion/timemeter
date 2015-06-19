package com.simbirsoft.timemeter.ui.main;

import android.os.Bundle;

public interface SectionFragment {
    public static final String ARG_SECTION_ID = "MainFragment_section_id";

    int getSectionId();
    String getFragmentStateKey();
}
