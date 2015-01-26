package com.simbirsoft.timemeter.ui.base;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.log.LogFactory;

import org.androidannotations.annotations.EActivity;
import org.slf4j.Logger;

@EActivity(R.layout.activity_fragment_container)
public class DialogContainerActivity extends FragmentContainerActivity {

    private static final Logger LOG = LogFactory.getLogger(DialogContainerActivity.class);

    private static final String TAG_CONTENT_FRAGMENT = "DialogContainerActivity_content_fragment_tag_";

    public static Intent prepareDialogLaunchIntent(Context packageContext,
                                                   String fragmentName, Bundle fragmentArgs) {

        Intent intent = new Intent(packageContext, DialogContainerActivity_.class);
        intent.putExtra(FragmentContainerActivity.EXTRA_FRAGMENT_NAME, fragmentName);

        if (fragmentArgs != null) {
            intent.putExtra(FragmentContainerActivity.EXTRA_FRAGMENT_ARGS, fragmentArgs);
        }

        return intent;
    }

    @Override
    protected void initContentView(String fragmentName, Bundle fragmentArgs) {
        try {
            DialogFragment fragment = (DialogFragment) Fragment.instantiate(
                    this, fragmentName, fragmentArgs);
            fragment.show(getSupportFragmentManager(), TAG_CONTENT_FRAGMENT);

        } catch (ClassCastException e) {
            LOG.error("specified fragment '{}' should be assignable from '{}'",
                    fragmentName, BaseDialogFragment.class.getName());

            throw e;
        }
    }
}
