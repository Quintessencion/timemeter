package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.log.LogFactory;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.apmem.tools.layouts.FlowLayout;
import org.slf4j.Logger;

import java.util.List;
import java.util.Stack;

@EViewGroup(R.layout.view_tag_flow)
public class TagFlowView extends FlowLayout {
    private static final Logger LOG = LogFactory.getLogger(TagView.class);

    private final Stack<View> mReuseTagViews = new Stack<>();
    private FlowLayout tagContainerView;

    public TagFlowView(Context context) {
        super(context);
    }

    public TagFlowView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public TagFlowView(Context context, AttributeSet attributeSet, int defaultStyleAttribute) {
        super(context, attributeSet, defaultStyleAttribute);
    }

    @AfterViews
    void initializeView() {
        tagContainerView = (FlowLayout) findViewById(R.id.tagFlowViewContainer);
    }

    public void bindTagViews(List<Tag> tags) {
        final int tagCount = tags.size();
        final View[] reuseViews = new View[tagCount];

        final int reuseViewCount = tagContainerView.getChildCount();
        for (int i = 0; i < reuseViewCount; i++) {
            mReuseTagViews.add(tagContainerView.getChildAt(i));
        }
        tagContainerView.removeAllViewsInLayout();

        for (int i = 0; i < tagCount; i++) {
            if (mReuseTagViews.isEmpty()) {
                reuseViews[i] = TagView_.build(tagContainerView.getContext());
            } else {
                reuseViews[i] = mReuseTagViews.pop();
            }

            tagContainerView.addView(reuseViews[i]);
        }

        if (tagCount > 0) {
            for (int i = 0; i < tagCount; i++) {
                Tag tag = tags.get(i);
                TagView_ tagView = (TagView_) reuseViews[i];
                tagView.setTagText(tag.getName());
                tagView.setTagColor(tag.getColor());
            }
            tagContainerView.setVisibility(View.VISIBLE);
        } else {
            tagContainerView.setVisibility(View.GONE);
        }
    }
}
