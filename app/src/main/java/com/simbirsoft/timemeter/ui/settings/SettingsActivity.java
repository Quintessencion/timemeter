package com.simbirsoft.timemeter.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.transitions.everywhere.Fade;
import android.transitions.everywhere.TransitionManager;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.base.BaseActivity;
import com.simbirsoft.timemeter.ui.base.FragmentContainerCallbacks;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_fragment_container)
public class SettingsActivity extends BaseActivity implements FragmentContainerCallbacks {

    public static final String EXTRA_FRAGMENT_NAME = "fragment_name";
    public static final String EXTRA_FRAGMENT_ARGS = "fragment_args";

    public static final int RESULT_CODE_PREFERENCES_MODIFIED = 2000;
    public static final int REQUEST_CODE_PREFERENCE_SCREEN = 200;

    private static final String TAG_CONTENT_FRAGMENT = "SettingsActivity_content_fragment_tag_";

    @Extra(EXTRA_FRAGMENT_NAME)
    String fragmentName;

    @Extra(EXTRA_FRAGMENT_ARGS)
    Bundle fragmentArgs;

    @ViewById(R.id.fragmentContainer)
    ViewGroup mFragmentContainer;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    public static Intent prepareLaunchIntent(Context packageContext, String fragmentName, Bundle fragmentArgs) {
        Intent intent = new Intent(packageContext, SettingsActivity_.class);
        intent.putExtra(SettingsActivity.EXTRA_FRAGMENT_NAME, fragmentName);

        if (fragmentArgs != null) {
            intent.putExtra(SettingsActivity.EXTRA_FRAGMENT_ARGS, fragmentArgs);
        }

        return intent;
    }

    @AfterViews
    void bindViews() {
        setSupportActionBar(mToolbar);
        Fragment fragment = getContentFragment();
        if (fragment == null) {
            initContentView(fragmentName, fragmentArgs);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar bar = getSupportActionBar();
        if (bar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment fragment = getContentFragment();

        switch (item.getItemId()) {
            case android.R.id.home:
                if (fragment != null && fragment.onOptionsItemSelected(item)) {
                    return true;
                }
                finish();
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = getContentFragment();

        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void initContentView(String fragmentName, Bundle fragmentArgs) {
        Fragment fragment = Fragment.instantiate(this, fragmentName, fragmentArgs);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, fragment, TAG_CONTENT_FRAGMENT)
                .commit();
    }

    protected SettingsFragment getContentFragment() {
        return (SettingsFragment) getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
    }

    @Override
    public void hideToolbar() {
        TransitionManager.beginDelayedTransition(mFragmentContainer, new Fade(Fade.OUT));
        mToolbar.setVisibility(View.GONE);
    }

    @Override
    public void showToolbar() {
        TransitionManager.beginDelayedTransition(mFragmentContainer, new Fade(Fade.OUT));
        mToolbar.setVisibility(View.VISIBLE);
    }
}
