package com.simbirsoft.timeactivity.events;

public class ScreenLockStateChangedEvent {
    public boolean isScreenLocked;

    public ScreenLockStateChangedEvent(boolean isScreenLocked) {
        this.isScreenLocked = isScreenLocked;
    }
}
