package com.simbirsoft.timeactivity.events;

/**
 * Created by e.gubin on 25.05.15.
 */
public class HelpCardPresentedEvent {
    private int mCardId;
    private boolean mIsPresented;

    public HelpCardPresentedEvent(int cardId, boolean isPresented) {
        mCardId = cardId;
        mIsPresented = isPresented;
    }

    public int getCardId() {
        return mCardId;
    }

    public boolean isPresented() {
        return mIsPresented;
    }
}
