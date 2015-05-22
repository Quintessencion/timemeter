package com.simbirsoft.timemeter.ui.views;

import android.content.Context;

import java.util.ArrayList;

public class HelpCardDataSource implements HelpCard.Adapter {

    private static class Item {
        public String message;
        public String actionTitle;
        public String nextTitle;

        public Item(String message, String actionTitle, String nextTitle) {
            this.message = message;
            this.actionTitle = actionTitle;
            this.nextTitle = nextTitle;
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

    public void addItem(String message, String actionTitle, String nextTitle) {
        items.add(new Item(message, actionTitle, nextTitle));
    }

    public void addItem(Context ctx, int message, int actionTitle, int nextTitle) {
        String msg = message > 0 ? ctx.getString(message) : "";
        String action = actionTitle > 0 ? ctx.getString(actionTitle) : "";
        String next = nextTitle > 0 ? ctx.getString(nextTitle) : "";
        items.add(new Item(msg, action, next));
    }
}
