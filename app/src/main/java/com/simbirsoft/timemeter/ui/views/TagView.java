package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.main.SectionFragmentContainer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

@EViewGroup(R.layout.view_tag)
public class TagView extends FrameLayout {
    private static final Logger LOG = LogFactory.getLogger(TagView.class);

    @ViewById(R.id.viewTagTitle)
    protected TextView textView;

    private Tag mTag;

    public interface TagViewClickListener {
        public void onClick(Tag tag);
    }

    public TagView(Context context) {
        super(context);
    }

    public TagView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public TagView(Context context, AttributeSet attributeSet, int defaultStyleAttribute) {
        super(context, attributeSet, defaultStyleAttribute);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TagView(Context context, AttributeSet attributeSet, int defaultStyleAttribute, int defaultStyleResource) {
        super(context, attributeSet, defaultStyleAttribute, defaultStyleResource);
    }

    @AfterViews
    void initializeView() {
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    public void setTag(Tag tag) {
        if (tag != null) {
            mTag = tag;
            GradientDrawable bg = (GradientDrawable) textView.getBackground();
            bg.setColor(tag.getColor());
            textView.setText(tag.getName());
        }
    }
/*
    public void setTagColor(int tagColor) {
        if (textView != null) {
            GradientDrawable bg = (GradientDrawable) textView.getBackground();
            bg.setColor(tagColor);
        }
    }

    public void setTagText(String tagText) {
        if (textView != null) {
            textView.setText(tagText);
        }
    }
    */
}
