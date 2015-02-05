package com.simbirsoft.timemeter.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.simbirsoft.timemeter.NavigationDrawerFragment;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.base.BaseActivity;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.stats.StatsFragment_;
import com.simbirsoft.timemeter.ui.tags.TagListFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

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
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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

}
