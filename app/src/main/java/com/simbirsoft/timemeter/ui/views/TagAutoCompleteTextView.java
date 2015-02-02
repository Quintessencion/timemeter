package com.simbirsoft.timemeter.ui.views;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.ui.util.TagViewUtils;
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
        Tag tag = (Tag) o;

        TextView view = TagViewUtils.inflateTagView(
                LayoutInflater.from(getContext()),
                (ViewGroup) getParent(),
                tag.getColor());
        view.setText(tag.getName());

        return view;
    }

    @Override
    protected Object defaultObject(String s) {
        Tag tag = new Tag();
        tag.setName(s);

        return tag;
    }
}
