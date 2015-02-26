package com.simbirsoft.timemeter.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;

import java.util.Collection;
import java.util.List;

public class NavigationDrawerListAdapter extends BaseAdapter {

    public static class NavigationItem {
        private int mDrawableId;
        private String mText;

        public int getDrawableId() {
            return mDrawableId;
        }

        public void setDrawableId(int drawableId) {
            mDrawableId = drawableId;
        }

        public String getText() {
            return mText;
        }

        public void setText(String text) {
            mText = text;
        }
    }

    private final List<NavigationItem> mItems;

    public NavigationDrawerListAdapter() {
        mItems = Lists.newArrayList();
    }

    public void setItems(Collection<NavigationItem> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public NavigationItem getItem(int pos) {
        return mItems.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        final NavigationItem item = getItem(pos);

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_drawer_navigation_item, parent, false);
        }

        TextView titleView = (TextView) convertView.findViewById(android.R.id.title);
        ImageView iconView = (ImageView) convertView.findViewById(android.R.id.icon);

        titleView.setText(item.getText());
        iconView.setImageResource(item.getDrawableId());

        return convertView;
    }
}
