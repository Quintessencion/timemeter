package com.simbirsoft.timemeter.ui.model;

import android.content.Intent;

public class TaskChangedEvent {
    private int mResultCode;
    private Intent mData;

    public TaskChangedEvent(int resultCode, Intent data) {
        mResultCode = resultCode;
        mData = data;
    }

    public int getResultCode() {
        return mResultCode;
    }

    public Intent getData() {
        return mData;
    }
}
