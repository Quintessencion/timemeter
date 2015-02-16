package com.simbirsoft.timemeter.ui.main;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.transitions.everywhere.ChangeBounds;
import android.transitions.everywhere.Fade;
import android.transitions.everywhere.TransitionManager;
import android.transitions.everywhere.TransitionSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import com.astuetz.PagerSlidingTabStrip;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.views.FilterView;
import com.tokenautocomplete.TokenCompleteTextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.slf4j.Logger;

import java.util.Calendar;
import java.util.List;

@EFragment(R.layout.fragment_tasks_pager)
public class MainPagerFragment extends MainFragment implements FilterViewProvider,
        TokenCompleteTextView.TokenListener,
        FilterView.OnSelectDateClickListener,
        DatePickerDialog.OnDateSetListener {

    interface PagesProvides {
        List<String> getPageNames(int sectionId);
    }

    private static final Logger LOG = LogFactory.getLogger(MainPagerFragment.class);
    private static final String TAG_DATE_PICKER_FRAGMENT = "main_date_picker_fragment_tag";

    @ViewById(R.id.pager)
    ViewPager mViewPager;

    @ColorRes(android.R.color.white)
    int mColorWhite;

    @InstanceState
    boolean mIsFilterPanelShown;

    private PagerSlidingTabStrip mTabs;
    private FilterView mFilterView;
    private MainPagerAdapter mPagerAdapter;
    private RelativeLayout mContainerUnderlayView;
    private ViewGroup mContainerView;
    private ViewGroup mContentRootView;
    private ViewGroup mContainerHeader;
    private Menu mOptionsMenu;
    private ContentFragmentCallbacks mContainerCallbacks;
    private List<String> mPageNames;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mContainerCallbacks = (ContentFragmentCallbacks) activity;
        int sectionId = getSectionId();
        mPageNames = Lists.newArrayList(((PagesProvides) activity).getPageNames(sectionId));
    }

    @Override
    public void onDetach() {
        mContainerCallbacks = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setShouldSubscribeForJobEvents(false);
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        DatePickerDialog dialog = (DatePickerDialog)
                getChildFragmentManager().findFragmentByTag(TAG_DATE_PICKER_FRAGMENT);
        if (dialog != null) {
            dialog.setOnDateSetListener(this);
        }
    }

    @AfterViews
    void bindViews() {
        mContainerUnderlayView = mContainerCallbacks.getContainerUnderlayView();
        mContainerView = mContainerCallbacks.getContainerView();
        mContentRootView = mContainerCallbacks.getContentRootView();
        mContainerHeader = mContainerCallbacks.getContainerHeaderView();
        mFilterView = (FilterView) LayoutInflater.from(getActivity())
                .inflate(R.layout.view_filter_impl, mContainerUnderlayView, false);
        mFilterView.setVisibility(View.INVISIBLE);
        mContainerUnderlayView.addView(mFilterView);

        if (mIsFilterPanelShown) {
            showFilterView(false);
        } else {
            hideFilterView(false);
        }

        mFilterView.setTokenListener(this);
        mFilterView.setOnSelectDateClickListener(this);

        mPagerAdapter = new MainPagerAdapter(getResources(), getChildFragmentManager());
        if (mPageNames != null) {
            List<Fragment> pages = Lists.newArrayListWithCapacity(mPageNames.size());
            for (String pageFragmentName : mPageNames) {
                Fragment pageFragment = Fragment.instantiate(getActivity(), pageFragmentName);
                pages.add(pageFragment);
                LOG.debug("page created: {}", pageFragment.getClass().getSimpleName());
            }
            mPagerAdapter.addFragments(pages);
        }
        mViewPager.setAdapter(mPagerAdapter);

        LayoutInflater.from(getActivity()).inflate(R.layout.view_tabs, mContainerHeader, true);
        mTabs = (PagerSlidingTabStrip) mContainerHeader.findViewById(R.id.tabs);
        mTabs.setTextColor(mColorWhite);
        mTabs.setViewPager(mViewPager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Hide custom elevation on Lollipop
            mContainerHeader.findViewById(R.id.shadowDown).setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        if (mFilterView != null) {
            mFilterView.setTokenListener(null);
            mFilterView.setOnSelectDateClickListener(null);
        }
        if (mFilterView != null) {
            mContainerUnderlayView.removeView(mFilterView);
        }
        mContainerHeader.removeAllViews();
        mContentRootView = null;
        mContainerUnderlayView = null;
        mContainerView = null;
        mContainerHeader = null;
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.main, menu);
        mOptionsMenu = menu;
        updateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.actionToggleFilter:
                toggleFilterView();
                updateOptionsMenu();
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showFilterView(boolean animate) {
        mIsFilterPanelShown = true;

        if (isFilterPanelVisible()) {
            return;
        }

        if (animate) {
            TransitionSet set = new TransitionSet();
            set.addTransition(new Fade(Fade.IN));
            set.addTransition(new ChangeBounds());
            set.setInterpolator(new DecelerateInterpolator(0.8f));
            set.setOrdering(TransitionSet.ORDERING_TOGETHER);
            set.excludeTarget(R.id.floatingButton, true);
            TransitionManager.beginDelayedTransition(mContentRootView, set);
        }

        updateFilterViewSize();
        mFilterView.setVisibility(View.VISIBLE);
    }

    private void hideFilterView(boolean animate) {
        mIsFilterPanelShown = false;

        if (!isFilterPanelVisible()) {
            return;
        }

        if (animate) {
            TransitionSet set = new TransitionSet();
            set.addTransition(new Fade(Fade.OUT));
            set.addTransition(new ChangeBounds());
            set.setInterpolator(new DecelerateInterpolator(0.8f));
            set.setOrdering(TransitionSet.ORDERING_TOGETHER);
            set.excludeTarget(R.id.floatingButton, true);
            TransitionManager.beginDelayedTransition(mContentRootView, set);
        }

        updateFilterViewSize();
        mFilterView.setVisibility(View.INVISIBLE);
    }

    private void updateFilterViewSize() {
        RelativeLayout.LayoutParams containerLayoutParams =
                (RelativeLayout.LayoutParams) mContainerView.getLayoutParams();

        int measuredHeight;
        if (mIsFilterPanelShown) {
            measuredHeight = mFilterView.getMeasuredHeight();
            if (measuredHeight < 1) {
                int maxHeight = getResources().getDisplayMetrics().heightPixels;
                int spec = View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST);
                mFilterView.measure(spec, spec);
                measuredHeight = mFilterView.getMeasuredHeight();
            }
        } else {
            measuredHeight = 0;
        }
        containerLayoutParams.topMargin = measuredHeight;
        mContainerView.setLayoutParams(containerLayoutParams);
    }

    private boolean isFilterPanelVisible() {
        return mFilterView != null && mFilterView.getVisibility() == View.VISIBLE;
    }

    private void toggleFilterView() {
        if (isFilterPanelVisible()) {
            hideFilterView(true);
        } else {
            showFilterView(true);
        }
    }

    private void updateOptionsMenu() {
        if (mOptionsMenu == null) {
            return;
        }

        MenuItem item = mOptionsMenu.findItem(R.id.actionToggleFilter);
        if (item == null) {
            return;
        }

        if (isFilterPanelVisible()) {
            item.setIcon(R.drawable.ic_visibility_off_white_24dp);
            item.setTitle(R.string.action_toggle_filter_off);
        } else {
            item.setIcon(R.drawable.ic_visibility_white_24dp);
            item.setTitle(R.string.action_toggle_filter_on);
        }
    }

    @Override
    public FilterView getFilterView() {
        return mFilterView;
    }

    @Override
    public void hideFilterView() {
        if (isFilterPanelVisible()) {
            hideFilterView(true);
        }
    }

    @Override
    public void onSelectDateClicked(Calendar selectedDate) {
        DatePickerDialog dialog = DatePickerDialog.newInstance(
                this,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH),
                false);
        dialog.show(getChildFragmentManager(), TAG_DATE_PICKER_FRAGMENT);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        if (!isFilterPanelVisible()) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        long date = calendar.getTimeInMillis();
        mFilterView.setDate(date);
    }

    @Override
    public void onTokenAdded(Object o) {
        mViewPager.post(this::updateFilterViewSize);
    }

    @Override
    public void onTokenRemoved(Object o) {
        mViewPager.post(this::updateFilterViewSize);
    }
}