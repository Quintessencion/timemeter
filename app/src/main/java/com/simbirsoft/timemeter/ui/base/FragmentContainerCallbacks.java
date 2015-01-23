package com.simbirsoft.timemeter.ui.base;

import android.content.Intent;

public interface FragmentContainerCallbacks {
    void finish();
    void setResult(int resultCode, Intent data);
}
