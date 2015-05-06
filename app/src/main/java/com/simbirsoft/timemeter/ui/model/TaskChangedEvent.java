package com.simbirsoft.timemeter.ui.model;

public class TaskChangedEvent {
    private int mResultCode;
    private int mSender;

    public TaskChangedEvent(int resultCode, int sender) {
        mResultCode = resultCode;
        mSender = sender;
    }

    public int getResultCode() {
        return mResultCode;
    }

    public int getSender() {
        return mSender;
    }
}
