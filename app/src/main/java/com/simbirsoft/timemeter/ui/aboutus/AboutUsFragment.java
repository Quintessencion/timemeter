package com.simbirsoft.timemeter.ui.aboutus;

import android.os.Bundle;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.main.MainFragment;

import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_about_us)
public class AboutUsFragment extends MainFragment{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setShouldSubscribeForJobEvents(false);
        super.onCreate(savedInstanceState);
    }
}
