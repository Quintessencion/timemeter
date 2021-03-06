package com.simbirsoft.timeactivity.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.astuetz.PagerSlidingTabStrip;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.google.common.collect.Lists;
import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.db.Preferences;
import com.simbirsoft.timeactivity.events.FilterViewStateChangeEvent;
import com.simbirsoft.timeactivity.injection.Injection;
import com.simbirsoft.timeactivity.log.LogFactory;
import com.simbirsoft.timeactivity.ui.util.KeyboardUtils;
import com.simbirsoft.timeactivity.ui.views.FilterResultsView;
import com.simbirsoft.timeactivity.ui.views.FilterView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tokenautocomplete.TokenCompleteTextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.StringRes;
import org.slf4j.Logger;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

@EFragment(R.layout.fragment_tasks_pager)
public class MainPagerFragment extends MainFragment implements FilterViewResultsProvider,
        TokenCompleteTextView.TokenListener,
        FilterView.OnSelectDateClickListener,
        DatePickerDialog.OnDateSetListener {

    interface PagesProvides {
        List<String> getPageNames(int sectionId);
    }

    private static final Logger LOG = LogFactory.getLogger(MainPagerFragment.class);
    private static final String TAG_DATE_PICKER_FRAGMENT = "main_date_picker_fragment_tag";

    public static final String ARG_NEED_SWITCH_TO_SELECTED_PAGE = "arg_need_switch_to_selected_page";
    public static final String ARG_PAGE_ID_FOR_SWITCHING = "arg_page_id_for_switching";

    @FragmentArg(ARG_NEED_SWITCH_TO_SELECTED_PAGE)
    protected boolean needSwitchToSelectedPage;

    @FragmentArg(ARG_PAGE_ID_FOR_SWITCHING)
    protected int pageId;

    @ViewById(R.id.pager)
    ViewPager mViewPager;

    @ViewById(R.id.searchResultContainer)
    FrameLayout mSearchResultContainer;

    @ColorRes(android.R.color.white)
    int mColorWhite;

    @InstanceState
    boolean mIsFilterPanelShown;

    @InstanceState
    boolean mIsFilterResultsPanelShown;

    @Inject
    Bus mBus;

    @InstanceState
    boolean mIsSearchViewExpanded;

    @InstanceState
    FilterView.FilterState mFilterState;

    @Inject
    Preferences mPrefs;

    @StringRes(R.string.search_by_name)
    String mSearchHint;

    private PagerSlidingTabStrip mTabs;
    private FilterView mFilterView;
    private FilterResultsView mFilterResultsView;
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
        try {
            SectionFragmentContainer container = (SectionFragmentContainer) activity;
            container.onSectionAttached(getSectionId());
        } catch (ClassCastException e) {
            throw new RuntimeException(String.format("%s should implement %s", activity.getClass().getName(), SectionFragmentContainer.class.getName()));
        }

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

        Injection.sUiComponent.injectMainPagerFragment(this);

        setHasOptionsMenu(true);

        mBus.register(this);

        if (mFilterState == null) {
            mFilterState = mPrefs.getFilterState();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFilterView != null) {
            mFilterView.updateDateView();
        }
    }

    @Override
    public void onDestroy() {
        mBus.unregister(this);
        savePagePosition();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        final FilterView.FilterState filterState = mFilterView.getViewFilterState();
        mPrefs.setFilterState(filterState);
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

        mFilterResultsView = (FilterResultsView) LayoutInflater.from(getActivity())
                .inflate(R.layout.view_filter_results_impl, mSearchResultContainer, false);
        mFilterResultsView.setVisibility(View.INVISIBLE);
        mSearchResultContainer.addView(mFilterResultsView);

        if (mIsFilterPanelShown) {
            showFilterView(false);
        } else {
            hideFilterView(false);
        }

        if (mIsFilterResultsPanelShown) {
            showSearchResultsPanel(false);
        } else {
            hideSearchResultsPanel(false);
        }

        if (mFilterView != null && mFilterState != null) {
            mFilterView.setFilterState(mFilterState);
        }

        mFilterView.setTokenListener(this);
        mFilterView.setOnSelectDateClickListener(this);

        mPagerAdapter = new MainPagerAdapter(getActivity(), getChildFragmentManager(), R.id.pager);
        mPagerAdapter.setOnSetupItemListener(this::onAdapterSetupItem);
        if (mPageNames != null) {
            mPagerAdapter.setPages(Lists.transform(mPageNames, PageItem::create));
            mViewPager.setOffscreenPageLimit(2);
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
        mTabs.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                onPageChanged(position);
            }
        });

        DatePickerDialog dialog = (DatePickerDialog)
                getChildFragmentManager().findFragmentByTag(TAG_DATE_PICKER_FRAGMENT);
        if (dialog != null) {
            dialog.setOnDateSetListener(this);
        }

        mTabs.post(() -> {
            onPageChanged(mViewPager.getCurrentItem());
            restorePagePosition();
        });
    }

    private void onAdapterSetupItem(Fragment fragment) {
        MainPageFragment mpf = (MainPageFragment)fragment;
        mpf.setFilterViewResultsProvider(this);
    }

    @Override
    public void onDestroyView() {
        hideFilterView(false);
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
        mSearchResultContainer = null;
        mContainerView = null;
        mContainerHeader = null;
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_main_pager, menu);
        mOptionsMenu = menu;

        final MenuItem searchItem = mOptionsMenu.findItem(R.id.actionSearch);
        if (mIsSearchViewExpanded) {
            searchItem.expandActionView();
        }
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                mIsSearchViewExpanded = true;

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                mIsSearchViewExpanded = false;

                return true;
            }
        });
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(mSearchHint);
        ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(view -> searchItem.collapseActionView());
        mFilterView.setSearchView(searchView);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mPagerAdapter != null && mPagerAdapter.getCount() > 0) {
            Fragment currentFragment = mPagerAdapter.getItem(mViewPager.getCurrentItem());
            currentFragment.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Subscribe
    public void onFitlerViewStateChanged(FilterViewStateChangeEvent event) {
        mFilterState = event.getFilterState();

        if (event.isReset()) {
            hideFilterView();
            updateOptionsMenu();
        }
    }

    private void showFilterView(boolean animate) {
        mIsFilterPanelShown = true;

        if (isFilterPanelVisible()) {
            return;
        }

        if (animate) {
            showTransition(Fade.IN);
        }

        updateContainerMargin();
        mFilterView.setVisibility(View.VISIBLE);
    }

    private void hideFilterView(boolean animate) {
        mIsFilterPanelShown = false;

        if (!isFilterPanelVisible()) {
            return;
        }

        if (animate) {
            showTransition(Fade.OUT);
        }

        updateContainerMargin();
        mFilterView.setVisibility(View.INVISIBLE);
        KeyboardUtils.hideSoftInput(mFilterView.getContext(), mFilterView.getWindowToken());
    }

    private boolean isFilterPanelVisible() {
        return mFilterView != null && mFilterView.getVisibility() == View.VISIBLE;
    }

    private boolean isFilterResultsPanelVisible() {
        return mFilterResultsView != null && mFilterResultsView .getVisibility() == View.VISIBLE;
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
            item.setIcon(R.drawable.ic_filter_remove_white_24dp);
            item.setTitle(R.string.action_toggle_filter_off);
        } else if (mFilterState != null && (mFilterState.isEmpty() || mFilterState.isFilteredBySearchText())) {
            item.setIcon(R.drawable.ic_filter_white_24dp);
            item.setTitle(R.string.action_toggle_filter_on);
        } else {
            item.setIcon(R.drawable.ic_filter_red_24dp);
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
    public void updateFilterResultsView(int taskCount, FilterView.FilterState filterState) {
        if (mFilterState == null) {
            return;
        }

        if (mFilterState.isEmpty()) {
            hideSearchResultsPanel(mIsFilterPanelShown);
        } else {
            mFilterResultsView.setSearchResultsState(new FilterResultsView.SearchResultsViewState(taskCount,
                    filterState.tags));
            showSearchResultsPanel(true);
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
        mViewPager.post(this::updateContainerMargin);
    }

    @Override
    public void onTokenRemoved(Object o) {
        mViewPager.post(this::updateContainerMargin);
    }

    private void savePagePosition() {
        mPrefs.setSelectedTaskTabPosition(mViewPager.getCurrentItem());
    }

    private void restorePagePosition() {
        if (needSwitchToSelectedPage) {
            needSwitchToSelectedPage = false;
            mViewPager.setCurrentItem(pageId);
        } else {
            mViewPager.setCurrentItem(mPrefs.getSelectedTaskTabPosition());
        }
    }

    private void onPageChanged(int position) {
        mPagerAdapter.deselectCurrentPage();
        MainPageFragment currentFragment = (MainPageFragment)mPagerAdapter.getItem(position);
        currentFragment.onPageSelected();
    }

    public void switchToSelectedPage(int pageId) {
        mViewPager.setCurrentItem(pageId);
        savePagePosition();
    }

    private void showSearchResultsPanel(boolean animate) {
        mIsFilterResultsPanelShown = true;

        if (isFilterResultsPanelVisible()) {
            return;
        }

        if (animate) {
            showTransition(Fade.IN);
        }

        updateContainerMargin();
        mFilterResultsView.setVisibility(View.VISIBLE);
    }

    private void hideSearchResultsPanel(boolean animate) {
        mIsFilterResultsPanelShown = false;

        if (!isFilterResultsPanelVisible()) {
            return;
        }

        if (animate) {
            showTransition(Fade.OUT);
        }

        updateContainerMargin();
        mFilterResultsView.setVisibility(View.INVISIBLE);
    }

    private void showTransition(int direction) {
        TransitionSet set = new TransitionSet();
        set.addTransition(new Fade(direction));
        set.addTransition(new ChangeBounds());
        set.setInterpolator(new DecelerateInterpolator(0.8f));
        set.setOrdering(TransitionSet.ORDERING_TOGETHER);
        set.excludeTarget(R.id.floatingButton, true);
        TransitionManager.beginDelayedTransition(mContentRootView, set);
    }

    private void updateContainerMargin() {
        LinearLayout.LayoutParams resultsContainerLayoutParams =
                (LinearLayout.LayoutParams) mSearchResultContainer.getLayoutParams();

        int filterPanelHeight;
        int resultsPanelHeight;

        if (mIsFilterPanelShown) {
            filterPanelHeight = mFilterView.getMeasuredHeight();
            if (filterPanelHeight < 1) {
                int spec = getMeasureSpec();
                mFilterView.measure(spec, spec);
                filterPanelHeight = mFilterView.getMeasuredHeight();
            }
        } else {
            filterPanelHeight = 0;
        }

        if (mIsFilterResultsPanelShown) {
            resultsPanelHeight = 0;
        } else {
            resultsPanelHeight = mFilterResultsView.getMeasuredHeight();

            if (resultsPanelHeight < 1) {
                int spec = getMeasureSpec();
                mFilterResultsView.measure(spec, spec);
                resultsPanelHeight = - mFilterResultsView.getMeasuredHeight();
            } else {
                resultsPanelHeight = - resultsPanelHeight;
            }
        }

        resultsContainerLayoutParams.topMargin = filterPanelHeight + resultsPanelHeight;
        mSearchResultContainer.setLayoutParams(resultsContainerLayoutParams);
    }

    private int getMeasureSpec() {
        int maxHeight = getResources().getDisplayMetrics().heightPixels;
        return View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST);
    }

    public boolean handleBackPress() {
        if (isFilterPanelVisible()) {
            hideFilterView(true);
            updateOptionsMenu();
            return true;
        }
        return false;
    }
}