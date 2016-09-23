package com.simbirsoft.timeactivity.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.common.collect.Lists;
import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.log.LogFactory;
import com.simbirsoft.timeactivity.ui.base.BaseActivity;
import com.simbirsoft.timeactivity.ui.calendar.ActivityCalendarFragment_;
import com.simbirsoft.timeactivity.ui.stats.StatsListFragment_;
import com.simbirsoft.timeactivity.ui.tags.TagListFragment_;
import com.simbirsoft.timeactivity.ui.tasklist.TaskListFragment_;
import com.simbirsoft.timeactivity.ui.views.FilterView;
import com.simbirsoft.timeactivity.ui.aboutus.AboutUsFragment_;
import com.simbirsoft.timeactivity.ui.settings.SettingsActivity;
import com.simbirsoft.timeactivity.ui.settings.SettingsFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        ContentFragmentCallbacks,
        FilterViewProvider,
        MainPagerFragment.PagesProvides,
        SectionFragmentContainer {

    private static final Logger LOG = LogFactory.getLogger(MainActivity.class);

    public static final String ACTION_SHOW_ACTIVE_TASK = "com.simbirsoft.android.intent.action.SHOW_ACTIVE_TASK";

    private static final String TAG_CONTENT_FRAGMENT = "app_content_fragment_tag";
    private static final int SECTION_ID_TASKS = 0;
    private static final int SECTION_ID_TAGS = 1;
    private static final int SECTION_ID_ABOUT_US = 2;
    private static final int SECTION_ID_SETTINGS = 3;

    private static final String KEY_FRAGMENT_STATE_KEY = "MainActivity_content_fragment_state_key";
    private static final String KEY_FRAGMENT_STATE = "MainActivity_content_fragment_state";

    private static final int SELECTED_PAGE_ID = 0;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @ViewById(R.id.taskListContentRoot)
    RelativeLayout mContentRoot;

    @ViewById(R.id.containerHeader)
    FrameLayout mContainerHeader;

    @ViewById(R.id.containerUnderlay)
    RelativeLayout mFragmentContainerUnderlay;

    @ViewById(R.id.container)
    FrameLayout mFragmentContainer;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @InstanceState
    ArrayList<Bundle> mSectionFragmentStates;

    @InstanceState
    boolean mIsTaskViewPending;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitle = getTitle();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @AfterViews
    void bindViews() {
        if (mSectionFragmentStates == null) {
            mSectionFragmentStates = Lists.newArrayList();
        }

        setSupportActionBar(mToolbar);
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, mDrawerLayout);

        if (ACTION_SHOW_ACTIVE_TASK.equals(getIntent().getAction())) {
            mIsTaskViewPending = true;
            mNavigationDrawerFragment.setCurrentSelectedPosition(SECTION_ID_TASKS);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mIsTaskViewPending = true;
        mNavigationDrawerFragment.setCurrentSelectedPosition(SECTION_ID_TASKS);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Fragment fragment = getContentFragment();
        if (fragment != null) {
            saveSectionFragmentState(fragment);
        }
    }

    private void setCurrentSection(int sectionId) {
        final boolean isViewTaskPending = mIsTaskViewPending;
        mIsTaskViewPending = false;
        Class<?> fragmentType;

        switch (sectionId) {
            case SECTION_ID_TASKS:
                fragmentType = MainPagerFragment_.class;
                break;

            case SECTION_ID_TAGS:
                fragmentType = TagListFragment_.class;
                break;

            case SECTION_ID_ABOUT_US:
                fragmentType = AboutUsFragment_.class;
                break;

            case SECTION_ID_SETTINGS:
                fragmentType = SettingsFragment_.class;
                break;

            default:
                LOG.error("unknown section selected");
                fragmentType = TaskListFragment_.class;
                break;
        }

        Fragment fragment = getContentFragment();
        if (fragment != null && fragmentType.equals(fragment.getClass())) {
            // selected fragment is already added
            if (isViewTaskPending && MainPagerFragment_.class.equals(fragment.getClass())) {
                ((MainPagerFragment)fragment).switchToSelectedPage(SELECTED_PAGE_ID);
            }
            return;
        }

        if (fragment != null) {
            saveSectionFragmentState(fragment);
        }

        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putInt(MainFragment.ARG_SECTION_ID, sectionId);
        fragmentArgs.putBoolean(MainPagerFragment.ARG_NEED_SWITCH_TO_SELECTED_PAGE, isViewTaskPending);
        fragmentArgs.putInt(MainPagerFragment.ARG_PAGE_ID_FOR_SWITCHING, SELECTED_PAGE_ID);
        fragment = Fragment.instantiate(this, fragmentType.getName(), fragmentArgs);

        Fragment.SavedState fragmentState = getSectionFragmentState(((SectionFragment)fragment));
        if (fragmentState != null) {
            fragment.setInitialSavedState(fragmentState);
        }

        if (fragmentType.equals(SettingsFragment_.class)) {
            Intent launchIntent = SettingsActivity.prepareLaunchIntent(
                    this, SettingsFragment_.class.getName(), fragmentArgs);
            startActivityForResult(launchIntent, SettingsActivity.REQUEST_CODE_PREFERENCE_SCREEN);
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment, TAG_CONTENT_FRAGMENT)
                    .commit();
        }
    }

    private Fragment getContentFragment() {
        return getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
    }

    @Override
    public void onNavigationDrawerItemSelected(int sectionId) {
        setCurrentSection(sectionId);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onSectionAttached(int number) {
        switch (number) {
            case SECTION_ID_TASKS:
                mTitle = getString(R.string.app_name);
                break;
            case SECTION_ID_TAGS:
                mTitle = getString(R.string.title_tags);
                break;
            case SECTION_ID_ABOUT_US:
                mTitle = getString(R.string.title_about_us);
                break;
            case SECTION_ID_SETTINGS:
                mTitle = getString(R.string.title_settings);
                break;
            default:
                LOG.error("unknown section title attached");
                mTitle = getString(R.string.title_tasks);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = getContentFragment();
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
            int sectionId = ((SectionFragment) fragment).getSectionId();
            if (requestCode == SettingsActivity.REQUEST_CODE_PREFERENCE_SCREEN) {
                mNavigationDrawerFragment.setCurrentSelectedPosition(sectionId);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public RelativeLayout getContainerUnderlayView() {
        return mFragmentContainerUnderlay;
    }

    @Override
    public FrameLayout getContainerHeaderView() {
        return mContainerHeader;
    }

    @Override
    public RelativeLayout getContentRootView() {
        return mContentRoot;
    }

    @Override
    public FrameLayout getContainerView() {
        return mFragmentContainer;
    }

    @Override
    public FilterView getFilterView() {
        Fragment fragment = getContentFragment();

        if (fragment instanceof FilterViewProvider) {
            return ((FilterViewProvider) fragment).getFilterView();
        }

        return null;
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.handleBackPress()) {
            return;
        }

        Fragment sectionFragment = getContentFragment();
        boolean isHandleBackPress = (sectionFragment instanceof MainFragment) ?
                ((MainFragment) sectionFragment).handleBackPress() :
                false;
        if (sectionFragment != null && isHandleBackPress) {
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void hideFilterView() {
        Fragment fragment = getContentFragment();

        if (fragment instanceof FilterViewProvider) {
            ((FilterViewProvider) fragment).hideFilterView();
        }
    }

    @Override
    public List<String> getPageNames(int sectionId) {
        List<String> pages = Lists.newArrayList();

        switch (sectionId) {
            case SECTION_ID_TASKS:
                pages.add(TaskListFragment_.class.getName());
                pages.add(StatsListFragment_.class.getName());
                pages.add(ActivityCalendarFragment_.class.getName());
                break;

            case SECTION_ID_TAGS:
                break;

            case SECTION_ID_ABOUT_US:
                break;

            case SECTION_ID_SETTINGS:
                break;

            default:
                LOG.error("unknown section pages requested: {}", sectionId);
                break;
        }

        return pages;
    }

    @Override
    protected View mainView() {
        return mDrawerLayout;
    }

    private Fragment.SavedState getSectionFragmentState(SectionFragment fragment) {
        return popFragmentState(fragment.getFragmentStateKey());
    }

    private void saveSectionFragmentState(Fragment fragment) {
        Fragment.SavedState state = getSupportFragmentManager().saveFragmentInstanceState(fragment);
        Bundle stateBundle = new Bundle();

        stateBundle.putParcelable(KEY_FRAGMENT_STATE, state);
        stateBundle.putString(KEY_FRAGMENT_STATE_KEY, ((SectionFragment)fragment).getFragmentStateKey());
        pushFragmentState(stateBundle);
    }

    private Fragment.SavedState popFragmentState(String fragmentStateKey) {
        if (mSectionFragmentStates == null) {
            return null;
        }

        if (TextUtils.isEmpty(fragmentStateKey)) {
            LOG.error("unable to find fragment state for empty fragment key");
            return null;
        }

        Iterator<Bundle> iter = mSectionFragmentStates.iterator();
        while (iter.hasNext()) {
            Bundle stateBundle = iter.next();

            if (stateBundle.get(KEY_FRAGMENT_STATE_KEY).equals(fragmentStateKey)) {
                iter.remove();

                return stateBundle.getParcelable(KEY_FRAGMENT_STATE);
            }
        }

        return null;
    }

    private void pushFragmentState(Bundle fragmentStateBundle) {
        if (mSectionFragmentStates == null) {
            return;
        }
        String fragmentStateKey = fragmentStateBundle.getString(KEY_FRAGMENT_STATE_KEY);

        if (TextUtils.isEmpty(fragmentStateKey)) {
            LOG.error("unable to save fragment state for empty fragment key");
            return;
        }

        popFragmentState(fragmentStateKey);

        mSectionFragmentStates.add(fragmentStateBundle);
    }
}