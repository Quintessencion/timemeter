package com.simbirsoft.timemeter.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Filter;

import com.google.common.base.Strings;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.log.LogFactory;
import com.tokenautocomplete.TokenCompleteTextView;

import org.slf4j.Logger;

public class TagFilterTextView extends TokenCompleteTextView {

    private static final Logger LOG = LogFactory.getLogger(TagFilterTextView.class);

    public interface VisibilityStateCallback {
        boolean isTagViewVisible();
    }

    /**
     * Special filter string used to retain all tags in the tags
     * view adapter except already selected tags
     *
     * No actual filtering is performed for empty filter strings,
     * so we need a special non-empty string
     */
    public static final String COMPLETION_TEXT_ALLOW_ANY = "*";

    private VisibilityStateCallback mVisibilityStateCallback;

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
        tagView.setTag((Tag) o);
        return tagView;
    }

    @Override
    protected Object defaultObject(String s) {
        Tag tag = new Tag();
        tag.setName(s);
        return tag;
    }

    @Override
    public boolean enoughToFilter() {
        if (mVisibilityStateCallback != null
                && !mVisibilityStateCallback.isTagViewVisible()) {

            return false;
        }

        //trigger suggestions in all cases except null adapter
        return (getAdapter() != null);
    }

    @Override
    protected void performFiltering(@NonNull CharSequence text, int start, int end, int keyCode) {
        LOG.error("perform filtering");
        String input = text.subSequence(start, end).toString();
        if (Strings.isNullOrEmpty(input)) {
            input = COMPLETION_TEXT_ALLOW_ANY;
        }

        Filter filter = getFilter();
        if (filter != null) {
            filter.filter(input, this);
        }
    }

    @Override
    protected String currentCompletionText() {
        final String current = super.currentCompletionText();

        return Strings.isNullOrEmpty(current) ? COMPLETION_TEXT_ALLOW_ANY : current;
    }

    public String getCurrentCompletionText() {
        return currentCompletionText();
    }

    public VisibilityStateCallback getVisibilityStateCallback() {
        return mVisibilityStateCallback;
    }

    public void setVisibilityStateCallback(VisibilityStateCallback visibilityStateCallback) {
        mVisibilityStateCallback = visibilityStateCallback;
    }
}
