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

@EViewGroup(R.layout.view_tag_flow)
public class TagFlowView extends FlowLayout {

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
        final int newTagCount = tags.size();
        final int oldTagCount = tagContainerView.getChildCount();
        final int diffCount = oldTagCount - newTagCount;

        if (diffCount > 0) {
            tagContainerView.removeViews(0, diffCount);
        } else {
            for(int i = diffCount; i < 0; i++) {
                tagContainerView.addView(TagView_.build(tagContainerView.getContext()));
            }
        }

        mTagViews.clear();
        if (newTagCount > 0) {
            for (int i = 0; i < newTagCount; i++) {
                TagView tagView = (TagView) tagContainerView.getChildAt(i);
                tagView.setTag(tags.get(i));
                mTagViews.add(tagView);
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
