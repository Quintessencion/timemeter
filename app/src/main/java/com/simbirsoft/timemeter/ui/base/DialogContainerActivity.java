package com.simbirsoft.timemeter.ui.base;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.Window;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.log.LogFactory;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.slf4j.Logger;

@EActivity(R.layout.activity_fragment_container)
public class DialogContainerActivity extends BaseActivity implements FragmentContainerCallbacks {

    public static final String EXTRA_FRAGMENT_NAME = "dialog_fragment_name";
    public static final String EXTRA_FRAGMENT_ARGS = "dialog_fragment_args";

    private static final Logger LOG = LogFactory.getLogger(DialogContainerActivity.class);

    private static final String TAG_CONTENT_FRAGMENT = "DialogContainerActivity_content_fragment_tag_";

    @Extra(EXTRA_FRAGMENT_NAME)
    String fragmentName;

    @Extra(EXTRA_FRAGMENT_ARGS)
    Bundle fragmentArgs;

    public static Intent prepareDialogLaunchIntent(
            Context packageContext, String fragmentName, Bundle fragmentArgs) {
        Intent intent = new Intent(packageContext, DialogContainerActivity_.class);
        intent.putExtra(DialogContainerActivity.EXTRA_FRAGMENT_NAME, fragmentName);

        if (fragmentArgs != null) {
            intent.putExtra(DialogContainerActivity.EXTRA_FRAGMENT_ARGS, fragmentArgs);
        }

        return intent;
    }

    @AfterViews
    void setupContent() {
        BaseDialogFragment fragment = getContentFragment();
        if (fragment == null) {
            initContentView(fragmentName, fragmentArgs);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        BaseDialogFragment fragment = getContentFragment();

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
        BaseDialogFragment fragment = getContentFragment();

        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void initContentView(String fragmentName, Bundle fragmentArgs) {
        BaseDialogFragment fragment = (BaseDialogFragment) Fragment.instantiate(
                this, fragmentName, fragmentArgs);
        fragment.show(getSupportFragmentManager(), TAG_CONTENT_FRAGMENT);
    }

    protected BaseDialogFragment getContentFragment() {
        return (BaseDialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
    }
}
