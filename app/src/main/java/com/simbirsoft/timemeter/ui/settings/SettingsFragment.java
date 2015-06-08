package com.simbirsoft.timemeter.ui.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.main.SectionFragment;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

public class SettingsFragment extends PreferenceFragment implements SectionFragment,
        TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference startTimePreference = getPreferenceScreen().findPreference("pref_startTime");

        startTimePreference.setOnPreferenceClickListener(preference -> {
            showTimePickerDialog();
            return true;
        });
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

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this,
                8,
                0,
                false,
                false);

        timePickerDialog.show(getChildFragmentManager(), "123");
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int i, int i1) {
        Toast.makeText(getActivity(), i + ":" + i1, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int i, int i1, int i2) {
        Toast.makeText(getActivity(), i + ":" + i1, Toast.LENGTH_SHORT).show();
    }
}
