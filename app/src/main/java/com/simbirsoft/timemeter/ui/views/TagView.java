package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.view_tag)
public class TagView extends FrameLayout {

    public interface TagViewClickListener {
        public void onClick(TagView tagView);
    }

    @ViewById(R.id.viewTagTitle)
    protected TextView textView;

    private TagViewClickListener mTagViewClickListener;
    private Tag mTag;

    public TagView(Context context) {
        super(context);
    }

    public TagView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public TagView(Context context, AttributeSet attributeSet, int defaultStyleAttribute) {
        super(context, attributeSet, defaultStyleAttribute);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TagView(Context context, AttributeSet attributeSet, int defaultStyleAttribute, int defaultStyleResource) {
        super(context, attributeSet, defaultStyleAttribute, defaultStyleResource);
    }

    @AfterViews
    void initializeView() {
    }

    public void setTag(@NonNull Tag tag) {
        mTag = tag;
        GradientDrawable bg = (GradientDrawable) textView.getBackground();
        bg.setColor(tag.getColor());
        textView.setText(tag.getName());
    }

    public Tag getTag() {
        return mTag;
    }

    public void setTagViewClickListener(TagViewClickListener tagViewClickListener) {
        mTagViewClickListener = tagViewClickListener;
        if (mTagViewClickListener != null) {
            textView.setOnClickListener( (v) -> {
                if (mTagViewClickListener != null) {
                    mTagViewClickListener.onClick(TagView.this);
                }
            });
        } else {
            textView.setOnClickListener(null);
        }
    }
}
