package com.simbirsoft.timemeter.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.NavigationDrawerFragment;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseActivity;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.stats.StatsFragment_;
import com.simbirsoft.timemeter.ui.stats.StatsListFragment_;
import com.simbirsoft.timemeter.ui.tasklist.TaskListFragment_;
import com.simbirsoft.timemeter.ui.views.FilterView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        ContentFragmentCallbacks,
        FilterViewProvider,
        MainPagerFragment.PagesProvides {

    private static final Logger LOG = LogFactory.getLogger(MainActivity.class);

    public static final String ACTION_SHOW_ACTIVE_TASK = "com.simbirsoft.android.intent.action.SHOW_ACTIVE_TASK";

    private static final String TAG_CONTENT_FRAGMENT = "app_content_fragment_tag";
    private static final int SECTION_ID_TASKS = 0;
    private static final int SECTION_ID_TAGS = 1;
    private static final int SECTION_ID_STATS = 2;

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

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitle = getTitle();
    }

    @AfterViews
    void bindViews() {
        setSupportActionBar(mToolbar);
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, mDrawerLayout);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // TODO: handle task activity intent from notification bar
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Class<?> fragmentType;

        switch (position) {
            case SECTION_ID_TASKS:
                fragmentType = MainPagerFragment_.class;
                break;

            case SECTION_ID_TAGS:
                fragmentType = MainPagerFragment_.class;
                break;

            case SECTION_ID_STATS:
                fragmentType = MainPagerFragment_.class;
                break;

            default:
                LOG.error("unknown section selected");
                fragmentType = TaskListFragment_.class;
                break;
        }

        MainFragment fragment = getContentFragment();
        if (fragment != null && fragmentType.equals(fragment.getClass())) {
            // selected fragment is already added
            return;
        }

        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putInt(MainFragment.ARG_SECTION_ID, position);
        fragment = (MainFragment) Fragment.instantiate(this, fragmentType.getName(), fragmentArgs);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, TAG_CONTENT_FRAGMENT)
                .commit();
    }

    private MainFragment getContentFragment() {
        return (MainFragment) getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_tasks);
                break;
            case 2:
                mTitle = getString(R.string.title_tags);
                break;
            case 3:
                mTitle = getString(R.string.title_stats);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BaseFragment fragment = getContentFragment();

        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
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
                break;

            case SECTION_ID_TAGS:
                break;

            case SECTION_ID_STATS:
                break;

            default:
                LOG.error("unknown section pages requested: {}", sectionId);
                break;
        }

        return pages;
    }
}
