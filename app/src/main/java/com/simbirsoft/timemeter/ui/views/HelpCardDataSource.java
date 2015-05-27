package com.simbirsoft.timemeter.ui.views;

import android.content.Context;

import com.simbirsoft.timemeter.R;

import java.util.ArrayList;

public class HelpCardDataSource implements HelpCard.Adapter {

    private static class Item {
        public String message;
        public String actionTitle;
        public String nextTitle;
        public String backTitle;

        public Item(String message, String actionTitle, String nextTitle, String backTitle) {
            this.message = message;
            this.actionTitle = actionTitle;
            this.nextTitle = nextTitle;
            this.backTitle = backTitle;
        }
    }

    private ArrayList<Item> items = new ArrayList<>();

    @Override
    public int getItemsCount() {
        return items.size();
    }

    @Override
    public String getMessage(int index) {
        return items.get(index).message;
    }

    @Override
    public String getTitleForActionButton(int index) {
        return items.get(index).actionTitle;
    }

    @Override
    public String getTitleForNextButton(int index) {
        return items.get(index).nextTitle;
    }

    @Override
    public String getTitleForBackButton(int index) {
        return items.get(index).backTitle;
    }

    public HelpCardDataSource addItem(String message, String actionTitle, String nextTitle, String backTitle) {
        items.add(new Item(message, actionTitle, nextTitle, backTitle));
        return this;
    }

    public HelpCardDataSource addItem(Context ctx, int message, int actionTitle, int nextTitle, int backTitle) {
        String msg = message > 0 ? ctx.getString(message) : "";
        String action = actionTitle > 0 ? ctx.getString(actionTitle) : "";
        String next = nextTitle > 0 ? ctx.getString(nextTitle) : "";
        String back = backTitle > 0 ? ctx.getString(backTitle) : "";
        return addItem(msg, action, next, back);
    }

    public HelpCardDataSource addItem(Context ctx, int message, int actionTitle, int nextTitle) {
        return addItem(ctx, message, actionTitle, nextTitle, R.string.help_card_btn_back);
    }
}
