package com.simbirsoft.timeactivity.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.db.model.Tag;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.view_tag)
public class TagView extends FrameLayout {

    public interface TagViewClickListener {
        public void onClick(TagView tagView);
    }

    @ViewById(R.id.tagTitle)
    protected TextView mTagTitle;

    @ViewById(R.id.tagPanel)
    protected LinearLayout mTagPanel;

    private View mVsTagImage;
    private TagViewClickListener mTagViewClickListener;
    private Tag mTag;
    private boolean mChecked;

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
        GradientDrawable bg = (GradientDrawable) mTagPanel.getBackground();
        bg.setColor(tag.getColor());
        mTagTitle.setText(tag.getName());
        highlightTag();
        mChecked = false;

        mTagPanel.post(new Runnable() {
            public void run() {
                // Post in the parent's message queue to make sure the parent
                // lays out its children before we call getHitRect()
                Rect delegateArea = new Rect();
                LinearLayout delegate = mTagPanel;
                delegate.getHitRect(delegateArea);
                delegateArea.top -= 2;
                delegateArea.bottom += 2;
                delegateArea.left -= 4;
                delegateArea.right += 4;
                TouchDelegate expandedArea = new TouchDelegate(delegateArea, delegate);
                // give the delegate to an ancestor of the view we're
                // delegating the area to
                if (View.class.isInstance(delegate.getParent())) {
                    ((View) delegate.getParent()).setTouchDelegate(expandedArea);
                }
            };
        });
    }

    public Tag getTag() {
        return mTag;
    }

    public void setTagViewClickListener(TagViewClickListener tagViewClickListener) {
        mTagViewClickListener = tagViewClickListener;
        if (mTagViewClickListener != null) {
            mTagPanel.setOnClickListener( (v) -> {
                if (mTagViewClickListener != null) {
                    mTagViewClickListener.onClick(TagView.this);
                }
            });
        } else {
            mTagPanel.setOnClickListener(null);
        }
    }

    public void enableTagImage() {
        if (mVsTagImage == null) {
            mVsTagImage = ((ViewStub) findViewById(R.id.vsTagImage)).inflate();
        } else {
            mVsTagImage.setVisibility(View.VISIBLE);
        }
    }

    public void disableTagImage() {
        if (mVsTagImage != null) {
            mVsTagImage.setVisibility(View.GONE);
        }
    }

    public void unhighlightTag() {
        mTagPanel.setAlpha(0.5f);
    }

    public void highlightTag() {
        mTagPanel.setAlpha(1.0f);
    }

    public  boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }
}
