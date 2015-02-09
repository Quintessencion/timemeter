package com.simbirsoft.timemeter.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.transitions.everywhere.ChangeBounds;
import android.transitions.everywhere.Fade;
import android.transitions.everywhere.Transition;
import android.transitions.everywhere.TransitionManager;
import android.transitions.everywhere.TransitionSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.simbirsoft.timemeter.NavigationDrawerFragment;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseActivity;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.base.ContentFragmentCallbacks;
import com.simbirsoft.timemeter.ui.stats.StatsFragment_;
import com.simbirsoft.timemeter.ui.tags.TagListFragment_;
import com.simbirsoft.timemeter.ui.views.FilterView;
import com.tokenautocomplete.TokenCompleteTextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, ContentFragmentCallbacks, TokenCompleteTextView.TokenListener {

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

    @ViewById(R.id.contentRoot)
    RelativeLayout mContentRoot;

    @ViewById(R.id.containerRoot)
    RelativeLayout mFragmentContainerRoot;

    @ViewById(R.id.container)
    FrameLayout mFragmentContainer;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @ViewById(R.id.filterView)
    FilterView mFilterView;

    @InstanceState
    boolean mIsFilterPanelShown;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Menu mOptionsMenu;

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

        if (mIsFilterPanelShown) {
            showFilterView(false);
        } else {
            hideFilterView(false);
        }
        mFilterView.setTokenListener(this);
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
                fragmentType = TaskListFragment_.class;
                break;

            case SECTION_ID_TAGS:
                fragmentType = TagListFragment_.class;
                break;

            case SECTION_ID_STATS:
                fragmentType = StatsFragment_.class;
                break;

            default:
                LOG.error("unknown section selected");
                fragmentType = TaskListFragment_.class;
                break;
        }

        Fragment fragment = getContentFragment();
        if (fragment != null && fragmentType.equals(fragment.getClass())) {
            // selected fragment is already added
            return;
        }

        fragment = Fragment.instantiate(this, fragmentType.getName());

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, TAG_CONTENT_FRAGMENT)
                .commit();
    }

    private BaseFragment getContentFragment() {
        return (BaseFragment) getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            mOptionsMenu = menu;
            updateOptionsMenu();
            restoreActionBar();
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void showFilterView(boolean animate) {
        mIsFilterPanelShown = true;
        if (animate) {
            TransitionSet set = new TransitionSet();
            set.addTransition(new Fade(Fade.IN));
            set.addTransition(new ChangeBounds());
            set.setInterpolator(new DecelerateInterpolator(0.8f));
            set.setOrdering(TransitionSet.ORDERING_TOGETHER);
            set.excludeTarget(R.id.floatingButton, true);
            TransitionManager.beginDelayedTransition(mContentRoot, set);
        }

        updateFilterViewSize();
        mFilterView.setVisibility(View.VISIBLE);
    }

    private void hideFilterView(boolean animate) {
        mIsFilterPanelShown = false;
        if (animate) {
            TransitionSet set = new TransitionSet();
            set.addTransition(new Fade(Fade.OUT));
            set.addTransition(new ChangeBounds());
            set.setInterpolator(new DecelerateInterpolator(0.8f));
            set.setOrdering(TransitionSet.ORDERING_TOGETHER);
            set.excludeTarget(R.id.floatingButton, true);
            TransitionManager.beginDelayedTransition(mContentRoot, set);
        }

        RelativeLayout.LayoutParams containerLayoutParams =
                (RelativeLayout.LayoutParams) mFragmentContainerRoot.getLayoutParams();
        containerLayoutParams.topMargin = 0;
        mFragmentContainerRoot.setLayoutParams(containerLayoutParams);
        mFilterView.setVisibility(View.INVISIBLE);
    }

    private void updateFilterViewSize() {
        RelativeLayout.LayoutParams containerLayoutParams =
                (RelativeLayout.LayoutParams) mFragmentContainerRoot.getLayoutParams();

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
        mFragmentContainerRoot.setLayoutParams(containerLayoutParams);
    }

    private boolean isFilterPanelVisible() {
        return mFilterView.getVisibility() == View.VISIBLE;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BaseFragment fragment = getContentFragment();

        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public RelativeLayout getFragmentContainerRoot() {
        return mFragmentContainerRoot;
    }

    @Override
    public FilterView getFilterView() {
        return mFilterView;
    }

    @Override
    public void hideFilterView() {
        if (isFilterPanelVisible()) {
            hideFilterView();
        }
    }

    @Override
    public void onTokenAdded(Object o) {
        mContentRoot.post(this::updateFilterViewSize);
    }

    @Override
    public void onTokenRemoved(Object o) {
        mContentRoot.post(this::updateFilterViewSize);
    }
}
