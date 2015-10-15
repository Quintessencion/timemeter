package com.simbirsoft.timeactivity.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.simbirsoft.timeactivity.db.model.Tag;
import com.tokenautocomplete.TokenCompleteTextView;

public class TagAutoCompleteTextView extends TokenCompleteTextView {

    public TagAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TagAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TagAutoCompleteTextView(Context context) {
        super(context);
    }

    @Override
    protected View getViewForObject(Object o) {
        TagView view = TagView_.build(getContext());
        view.setTag((Tag) o);
        return view;
    }

    @Override
    protected Object defaultObject(String s) {
        Tag tag = new Tag();
        tag.setName(s);

        return tag;
    }
}
