package com.simbirsoft.timemeter.ui.main;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

public class MainPagerAdapter extends FragmentPagerAdapter {

    private final Resources mResources;

    public static interface PageTitleProvider {
        String getPageTitle(Resources resources);
    }

    private final List<Fragment> mFragments;

    public MainPagerAdapter(Resources resources, FragmentManager fm) {
        super(fm);
        mResources = resources;

        mFragments = Lists.newArrayList();
    }

    public void addFragments(Collection<Fragment> fragments) {
        mFragments.addAll(fragments);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Fragment page = getItem(position);

        if (page instanceof PageTitleProvider) {
            return ((PageTitleProvider) page).getPageTitle(mResources);
        }

        return page.getClass().getSimpleName();
    }
}
