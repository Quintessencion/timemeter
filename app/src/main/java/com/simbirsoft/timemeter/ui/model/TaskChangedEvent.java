package com.simbirsoft.timemeter.ui.model;

public class TaskChangedEvent {
    private int mResultCode;

    public TaskChangedEvent(int resultCode) {
        mResultCode = resultCode;
    }

    public int getResultCode() {
        return mResultCode;
    }
}
