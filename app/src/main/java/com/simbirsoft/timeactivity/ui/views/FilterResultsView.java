package com.simbirsoft.timeactivity.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.db.model.Tag;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.Collections;
import java.util.List;

@EViewGroup(R.layout.view_filter_results)
public class FilterResultsView extends RelativeLayout {
    public static class SearchResultsViewState {

        public int mTaskCount;
        public List<Tag> mTags;

        public SearchResultsViewState(int taskCount, List<Tag> tags) {
            mTaskCount = taskCount;
            mTags = tags;
        }

        private SearchResultsViewState() {
        }

        public SearchResultsViewState copy() {
            SearchResultsViewState searchResultsViewState = new SearchResultsViewState();

            searchResultsViewState.mTaskCount = mTaskCount;
            searchResultsViewState.mTags = Lists.newArrayList(
                    mTags == null ? Collections.emptyList() : mTags);

            return searchResultsViewState;
        }
    }

    @ViewById(R.id.filter_results)
    TextView mSearchResultsTextView;

    Context mContext;

    SearchResultsViewState mSearchResultsViewState;

    public FilterResultsView(Context context) {
        super(context);
        mContext = context;
    }

    public FilterResultsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public FilterResultsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FilterResultsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    public void setSearchResultsState(SearchResultsViewState searchResultsViewState) {
        mSearchResultsViewState = searchResultsViewState.copy();
        updateView();
    }

    public void updateView() {
        if (mSearchResultsViewState == null) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(mContext.getResources().
                getQuantityString(R.plurals.task_count, mSearchResultsViewState.mTaskCount), mSearchResultsViewState.mTaskCount));

        if (mSearchResultsViewState.mTags != null && !mSearchResultsViewState.mTags.isEmpty()) {
            final String tags = Joiner.on(", ").join(Iterables.transform(mSearchResultsViewState.mTags, Tag::getName));
            stringBuilder.append(String.format(mContext.getResources().
                    getQuantityString(R.plurals.tags_count, mSearchResultsViewState.mTags.size()), tags));
        }

        mSearchResultsTextView.setText(stringBuilder.toString());
    }
}
