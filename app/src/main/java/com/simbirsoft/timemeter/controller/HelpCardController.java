package com.simbirsoft.timemeter.controller;

import android.app.Application;

import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.App;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.Preferences;
import com.simbirsoft.timemeter.events.HelpCardPresentedEvent;
import com.simbirsoft.timemeter.ui.helpcards.HelpCardDataSource;
import com.squareup.otto.Bus;

import java.util.Hashtable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HelpCardController {

    public final static int HELP_CARD_NONE = -1;
    public final static int HELP_CARD_TASK_LIST = 0;
    public final static int HELP_CARD_STATS_LIST = 1;
    public final static int HELP_CARD_CALENDAR = 2;
    public final static int HELP_CARD_DEMO_DATAS = 3;
    public final static int HELP_CARD_ADD_NEW_TASK = 4;
    public final static int HELP_CARD_TAGS = 5;

    private final Preferences mPreferences;
    private final Application mAppContext;
    private final Bus mBus;

    private final Hashtable<Integer, HelpCardDataSource> mCards = new Hashtable<>();

    @Inject
    public HelpCardController(App appContext, Preferences preferences, Bus bus) {
        mAppContext = appContext;
        mPreferences = preferences;
        mBus = bus;
        createCards();
    }

    private void createCards() {
        mCards.put(HELP_CARD_TASK_LIST, new HelpCardDataSource()
                .addItem(mAppContext, R.string.help_card_text_00_00, -1, R.string.help_card_btn_next)
                .addItem(mAppContext, R.string.help_card_text_00_01, -1, R.string.help_card_btn_ok));

        mCards.put(HELP_CARD_STATS_LIST, new HelpCardDataSource()
                .addItem(mAppContext, R.string.help_card_text_01_00, -1, R.string.help_card_btn_next)
                .addItem(mAppContext, R.string.help_card_text_01_01, -1, R.string.help_card_btn_ok));

        mCards.put(HELP_CARD_CALENDAR, new HelpCardDataSource()
                .addItem(mAppContext, R.string.help_card_text_02_00, -1, R.string.help_card_btn_ok));

        mCards.put(HELP_CARD_DEMO_DATAS, new HelpCardDataSource()
                .addItem(mAppContext, R.string.help_card_text_03_00, R.string.help_card_btn_delete, R.string.help_card_btn_hide));

        mCards.put(HELP_CARD_ADD_NEW_TASK, new HelpCardDataSource()
                .addItem(mAppContext, R.string.help_card_text_04_00, -1, R.string.help_card_btn_hide));

        mCards.put(HELP_CARD_TAGS, new HelpCardDataSource()
                .addItem(mAppContext, R.string.help_card_text_05_00, -1, R.string.help_card_btn_hide));
    }

    public HelpCardDataSource getCard(int cardId) {
        return mCards.get(cardId);
    }

    public void markPresented(int cardId) {
        List<Integer> presented = Lists.newArrayList(mPreferences.getPresentedHelpCards());
        presented.add(cardId);
        mPreferences.setPresentedHelpCards(presented.toArray(new Integer[0]));
        mBus.post(new HelpCardPresentedEvent(cardId, true));
    }

    public void markUnpresented(int cardId) {
        List<Integer> presented = Lists.newArrayList(mPreferences.getPresentedHelpCards());
        presented.remove(cardId);
        mPreferences.setPresentedHelpCards(presented.toArray(new Integer[0]));
        mBus.post(new HelpCardPresentedEvent(cardId, false));
    }

    public void markAllUnpresented() {
        mPreferences.setPresentedHelpCards(new Integer[0]);
        mBus.post(new HelpCardPresentedEvent(-1, false));
    }

    public boolean isPresented(int cardId) {
        List<Integer> presented = Lists.newArrayList(mPreferences.getPresentedHelpCards());
        return presented.contains(cardId);
    }

    public boolean isPresented(Integer... cardIds) {
        for (Integer id : cardIds) {
            if (!isPresented(id)) {
                return false;
            }
        }
        return true;
    }
}
