package com.simbirsoft.timemeter.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.ui.util.TagViewUtils;
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
        Tag tag = (Tag) o;

        View itemView = LayoutInflater.from(getContext())
                .inflate(R.layout.view_tag_filter_item, (ViewGroup) getParent(), false);

        View tagPanel = itemView.findViewById(R.id.tagPanel);
        TextView tagTitle = (TextView) itemView.findViewById(android.R.id.title);

        TagViewUtils.updateTagViewColor(tagPanel, tag.getColor());
        tagTitle.setText(tag.getName());

        return itemView;
    }

    @Override
    protected Object defaultObject(String s) {
        Tag tag = new Tag();
        tag.setName(s);

        return tag;
    }
}
