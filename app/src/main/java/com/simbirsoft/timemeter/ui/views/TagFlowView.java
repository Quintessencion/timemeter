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
    protected FlowLayout mTagContainerView;

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

    public void checkTagViews(List<Object> tagsFromFilter) {
        if (tagsFromFilter != null) {
            for (Object o : tagsFromFilter) {
                Tag tagFromFilter = (Tag) o;
                for (TagView tagView : mTagViews) {
                    if (tagFromFilter.getId() == tagView.getTag().getId()) {
                        tagView.checkTag();
                    }
                }
            }
        }
    }

    public void bindTagViews(List<Tag> tags) {
        final int newTagCount = tags.size();
        final int oldTagCount = mTagContainerView.getChildCount();
        final int diffCount = oldTagCount - newTagCount;

        if (diffCount > 0) {
            mTagContainerView.removeViews(0, diffCount);
        } else {
            for(int i = diffCount; i < 0; i++) {
                mTagContainerView.addView(TagView_.build(mTagContainerView.getContext()));
            }
        }

        mTagViews.clear();
        if (newTagCount > 0) {
            for (int i = 0; i < newTagCount; i++) {
                TagView tagView = (TagView) mTagContainerView.getChildAt(i);
                tagView.setTag(tags.get(i));
                mTagViews.add(tagView);
            }
            mTagContainerView.setVisibility(View.VISIBLE);
        } else {
            mTagContainerView.setVisibility(View.GONE);
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
