package com.simbirsoft.timeactivity.ui.main;

import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.db.Preferences;
import com.simbirsoft.timeactivity.events.ReadyToShowHelpCardEvent;
import com.simbirsoft.timeactivity.injection.Injection;
import com.squareup.otto.Bus;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.Arrays;

import javax.inject.Inject;

@EFragment(R.layout.fragment_navigation_drawer)
public class NavigationDrawerFragment extends Fragment {
    public static final int MENU_ITEMS_COUNT = 4;
    private static final int SETTINGS_ITEM_ID = 3;

    private NavigationDrawerCallbacks mCallbacks;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;
    private NavigationDrawerListAdapter mAdapter;

    @ViewById(android.R.id.list)
    ListView mDrawerListView;

    @ViewById(R.id.footerList)
    ListView mFooterListView;

    @Inject
    Bus mBus;

    @Inject
    Preferences mPreferences;

    private int mCurrentSelectedPosition = 0;


    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Injection.sUiComponent.injectNavigationDrawerFragment(this);

        loadCurrentSelectedPosition();
    }

    @Override
    public void onDestroy() {
        saveCurrentSelectedPosition();
        super.onDestroy();
    }

    public void setCurrentSelectedPosition(int currentSelectedPosition) {
        selectItem(currentSelectedPosition, isFooterSelected(currentSelectedPosition));
    }

    private void saveCurrentSelectedPosition() {
        if (mCurrentSelectedPosition != SETTINGS_ITEM_ID) {
            mPreferences.setSelectedSectionPosition(mCurrentSelectedPosition);
        }
    }

    private void loadCurrentSelectedPosition() {
        mCurrentSelectedPosition = mPreferences.getSelectedSectionPosition();
        selectItem(mCurrentSelectedPosition, isFooterSelected(mCurrentSelectedPosition));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @AfterViews
    void bindViews() {
        mAdapter = new NavigationDrawerListAdapter();

        NavigationDrawerListAdapter.NavigationItem tasks = getNavigationItem(R.drawable.ic_clock_32dp_selector, R.string.title_tasks);
        NavigationDrawerListAdapter.NavigationItem tags = getNavigationItem(R.drawable.ic_tag_32dp_selector, R.string.title_tags);
        NavigationDrawerListAdapter.NavigationItem aboutUs = getNavigationItem(R.drawable.ic_about_us_selector, R.string.title_about_us);
        NavigationDrawerListAdapter.NavigationItem settings = getNavigationItem(R.drawable.ic_settings_selector, R.string.title_settings);

        mAdapter.setItems(Arrays.asList(tasks, tags, aboutUs));

        mDrawerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mDrawerListView.setOnItemClickListener(
                (parent, view, position, id) -> selectItem(position, false));

        mDrawerListView.setAdapter(mAdapter);
        configureFooter(settings);
        setCheckedItem();
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mPreferences.getUserLearnedDrawer()) {
                    mPreferences.setUserLearnedDrawer(true);
                    mBus.post(new ReadyToShowHelpCardEvent());
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        if (!mPreferences.getUserLearnedDrawer()) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        mDrawerLayout.post(() -> mDrawerToggle.syncState());

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position, boolean isFooterView) {
        if (isFooterView) {
            position = MENU_ITEMS_COUNT - 1;
        }
        mCurrentSelectedPosition = position;

        if (isFooterView) {
            checkListViewItem(mFooterListView, 0);
            clearListViewSelection(mDrawerListView);
        } else {
            checkListViewItem(mDrawerListView, position);
            clearListViewSelection(mFooterListView);
        }

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public boolean handleBackPress() {
        if (isDrawerOpen()) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
            return true;
        }

        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mDrawerLayout != null && isDrawerOpen()) {
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private NavigationDrawerListAdapter.NavigationItem getNavigationItem(int drawableId, int titleId) {
        NavigationDrawerListAdapter.NavigationItem navigationItem = new NavigationDrawerListAdapter.NavigationItem();
        navigationItem.setDrawableId(drawableId);
        navigationItem.setText(getString(titleId));
        return navigationItem;
    }

    private void configureFooter(NavigationDrawerListAdapter.NavigationItem footerItem) {
        NavigationDrawerListAdapter footerAdapter = new NavigationDrawerListAdapter();

        footerAdapter.setItems(Arrays.asList(footerItem));

        mFooterListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mFooterListView.setOnItemClickListener(
                (parent, view, position, id) -> selectItem(position, true));

        mFooterListView.setAdapter(footerAdapter);
    }

    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private void checkListViewItem(ListView listView, int position) {
        if (listView != null) {
            listView.setItemChecked(position, true);
        }
    }

    private void clearListViewSelection(ListView listView) {
        if (listView != null) {
            listView.clearChoices();
        }
    }

    private void setCheckedItem() {
        if (isFooterSelected(mCurrentSelectedPosition)) {
            checkListViewItem(mFooterListView, 0);
        } else {
            checkListViewItem(mDrawerListView, mCurrentSelectedPosition);
        }
    }

    private boolean isFooterSelected(int position) {
        return position >= MENU_ITEMS_COUNT - 1;
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    public static interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(int position);
    }
}