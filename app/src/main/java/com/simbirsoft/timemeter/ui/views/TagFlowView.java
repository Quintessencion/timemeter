package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.apmem.tools.layouts.FlowLayout;

import java.util.List;
import java.util.Stack;

@EViewGroup(R.layout.view_tag_flow)
public class TagFlowView extends FlowLayout implements TagView.TagViewClickListener {

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

    @Override
    public void onClick(Tag tag) {
        // для отладки
        Toast.makeText(getContext(), tag.getName(), Toast.LENGTH_SHORT).show();
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
                TagView_ tagView = (TagView_) reuseViews[i];
                tagView.setTag(tags.get(i));
                tagView.setTagViewClickListener(this);
            }
            tagContainerView.setVisibility(View.VISIBLE);
        } else {
            tagContainerView.setVisibility(View.GONE);
        }
    }
}
