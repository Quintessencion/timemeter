package com.simbirsoft.timemeter.events;

public class ScreenLockStateChangedEvent {
    public boolean isScreenLocked;

    public ScreenLockStateChangedEvent(boolean isScreenLocked) {
        this.isScreenLocked = isScreenLocked;
    }
}
