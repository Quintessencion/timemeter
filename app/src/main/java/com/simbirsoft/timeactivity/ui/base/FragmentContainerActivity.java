package com.simbirsoft.timeactivity.ui.base;

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

import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.log.LogFactory;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;


@EActivity(R.layout.activity_fragment_container)
public class FragmentContainerActivity extends BaseActivity implements FragmentContainerCallbacks {

    public static final String EXTRA_FRAGMENT_NAME = "fragment_name";
    public static final String EXTRA_FRAGMENT_ARGS = "fragment_args";

    private static final Logger LOG = LogFactory.getLogger(FragmentContainerActivity.class);

    private static final String TAG_CONTENT_FRAGMENT = "FragmentContainerActivity_content_fragment_tag_";

    @Extra(EXTRA_FRAGMENT_NAME)
    String fragmentName;

    @Extra(EXTRA_FRAGMENT_ARGS)
    Bundle fragmentArgs;

    @ViewById(R.id.fragmentContainer)
    ViewGroup mFragmentContainer;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    public static Intent prepareLaunchIntent(Context packageContext, String fragmentName, Bundle fragmentArgs) {
        Intent intent = new Intent(packageContext, FragmentContainerActivity_.class);
        intent.putExtra(FragmentContainerActivity.EXTRA_FRAGMENT_NAME, fragmentName);

        if (fragmentArgs != null) {
            intent.putExtra(FragmentContainerActivity.EXTRA_FRAGMENT_ARGS, fragmentArgs);
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
        BaseFragment fragment = getContentFragment();

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
        BaseFragment fragment = getContentFragment();

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

    protected BaseFragment getContentFragment() {
        return (BaseFragment) getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
    }

    @Override
    public void onBackPressed() {
        BaseFragment fragment = getContentFragment();

        if (fragment != null && fragment.handleBackPress()) {
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected View mainView() {
        return mFragmentContainer;
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