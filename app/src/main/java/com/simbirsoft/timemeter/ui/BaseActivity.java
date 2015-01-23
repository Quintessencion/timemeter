package com.simbirsoft.timemeter.ui;

import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

public class BaseActivity extends ActionBarActivity {

    public void showToast(int text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

}
