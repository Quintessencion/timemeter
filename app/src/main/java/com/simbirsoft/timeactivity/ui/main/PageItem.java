package com.simbirsoft.timeactivity.ui.main;

import android.support.v4.app.Fragment;

import auto.parcel.AutoParcel;

@AutoParcel
abstract class PageItem {

    private Fragment mFragment;

    abstract String fragmentName();

    static PageItem create(String fragmentName) {
        return new AutoParcel_PageItem(fragmentName);
    }

    public Fragment getFragment() {
        return mFragment;
    }

    public void setFragment(Fragment fragment) {
        mFragment = fragment;
    }
}