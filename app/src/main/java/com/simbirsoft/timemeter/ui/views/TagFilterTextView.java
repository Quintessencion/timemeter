package com.simbirsoft.timemeter.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.simbirsoft.timemeter.db.model.Tag;
import com.tokenautocomplete.TokenCompleteTextView;

public class TagFilterTextView extends TokenCompleteTextView {

    public TagFilterTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TagFilterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TagFilterTextView(Context context) {
        super(context);
    }

    @Override
    protected View getViewForObject(Object o) {
        TagView tagView = TagView_.build(getContext());
        tagView.enableTagImage();
        tagView.setTag((Tag)o);
        return tagView;
    }

    @Override
    protected Object defaultObject(String s) {
        Tag tag = new Tag();
        tag.setName(s);
        return tag;
    }
}
