package com.simbirsoft.timemeter.ui.main;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

public class MainPagerAdapter extends FragmentPagerAdapter {

    public static interface PageTitleProvider {
        String getPageTitle(Resources resources);
    }

    private final Context mContext;
    private final FragmentManager mFragmentManager;
    private final int mPagerViewId;
    private final Resources mResources;

    private final List<PageItem> mPages;

    public MainPagerAdapter(Context context, FragmentManager fm, int pagerViewId) {
        super(fm);

        mContext = context;
        mFragmentManager = fm;
        mPagerViewId = pagerViewId;
        mResources = context.getResources();
        mPages = Lists.newArrayList();
    }

    public void setPages(Collection<PageItem> pages) {
        mPages.clear();
        mPages.addAll(pages);
        notifyDataSetChanged();
    }

    public PageItem getPage(int position) {
        Preconditions.checkPositionIndex(position, mPages.size());

        return mPages.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        PageItem page = getPage(position);

        Fragment pageFragment = page.getFragment();
        if (pageFragment == null) {
            pageFragment = mFragmentManager.findFragmentByTag(getPageFragmentTag(position));

            if (pageFragment == null) {
                pageFragment = Fragment.instantiate(mContext, page.fragmentName());
            }

            page.setFragment(pageFragment);
        }

        return pageFragment;
    }

    @Override
    public int getCount() {
        return mPages.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Fragment page = getItem(position);

        if (page instanceof PageTitleProvider) {
            return ((PageTitleProvider) page).getPageTitle(mResources);
        }

        return page.getClass().getSimpleName();
    }

    private String getPageFragmentTag(int position) {
        return "android:switcher:" + String.valueOf(mPagerViewId) + ":" + String.valueOf(position);
    }

    public void deselectCurrentPage() {
        for (PageItem item : mPages) {
            Fragment pageFragment = item.getFragment();
            if (pageFragment == null) continue;
            MainPageFragment mainPageFragment = (MainPageFragment)pageFragment;
            if (mainPageFragment.isSelected()) {
                mainPageFragment.onDeselect();
                break;
            }
        }
    }
}
