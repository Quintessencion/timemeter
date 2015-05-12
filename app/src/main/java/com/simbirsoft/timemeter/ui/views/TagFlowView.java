package com.simbirsoft.timemeter.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@EViewGroup(R.layout.view_tag_flow)
public class TagFlowView extends FlowLayout {

    private static final Stack<View> mReuseTagViews = new Stack<>();
    private final ArrayList<TagView> mTagViews = new ArrayList<>();

    @ViewById(R.id.tagFlowViewContainer)
    protected FlowLayout tagContainerView;

    public TagFlowView(Context context) {
        super(context);
    }

    public TagFlowView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public TagFlowView(Context context, AttributeSet attributeSet, int defaultStyleAttribute) {
        super(context, attributeSet, defaultStyleAttribute);
    }

    @AfterViews
    void initializeView() {
    }

    public void bindTagViews(List<Tag> tags) {
        final int tagCount = tags.size();

        final int reuseViewCount = tagContainerView.getChildCount();
        for (int i = 0; i < reuseViewCount; i++) {
            mReuseTagViews.add(tagContainerView.getChildAt(i));
        }
        tagContainerView.removeAllViewsInLayout();

        for (int i = 0; i < tagCount; i++) {
            if (mReuseTagViews.isEmpty()) {
                mTagViews.add(i, TagView_.build(tagContainerView.getContext()));
            } else {
                mTagViews.add(i, (TagView) mReuseTagViews.pop());
            }

            tagContainerView.addView(mTagViews.get(i));
        }

        if (tagCount > 0) {
            for (int i = 0; i < tagCount; i++) {
                TagView tagView = mTagViews.get(i);
                tagView.setTag(tags.get(i));
            }
            tagContainerView.setVisibility(View.VISIBLE);
        } else {
            tagContainerView.setVisibility(View.GONE);
        }
    }

    public ArrayList<TagView> getTagViews() {
        return mTagViews;
    }

    public void setTagViewsClickListener(TagView.TagViewClickListener tagViewClickListener) {
        for (TagView tagView : mTagViews) {
            tagView.setTagViewClickListener(tagViewClickListener);
        }
    }
}
