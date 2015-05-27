package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

@EViewGroup(R.layout.view_filter_results)
public class FilterResultsView extends RelativeLayout{
    @ViewById(R.id.filter_results)
    TextView mSearchResultsTextView;

    Context mContext;

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

    public void updateView(int foundTaskCount, FilterView.FilterState filterState) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(mContext.getResources().
                getQuantityString(R.plurals.task_count, foundTaskCount), foundTaskCount));

        if (filterState.tags != null && !filterState.tags.isEmpty()) {
            final String tags = Joiner.on(", ").join(Iterables.transform(filterState.tags, Tag::getName));
            stringBuilder.append(String.format(String.format(mContext.getResources().
                    getQuantityString(R.plurals.tags_count, filterState.tags.size()), tags)));
        }

        mSearchResultsTextView.setText(stringBuilder.toString());
    }
}
