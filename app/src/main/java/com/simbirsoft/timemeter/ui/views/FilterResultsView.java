package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.Collections;
import java.util.List;

@EViewGroup(R.layout.view_filter_results)
public class FilterResultsView extends RelativeLayout {
    public static class SearchResultsViewState implements Parcelable {

        public static final Creator<SearchResultsViewState> CREATOR =
                new Creator<SearchResultsViewState>() {
                    @Override
                    public SearchResultsViewState createFromParcel(Parcel parcel) {
                        return new SearchResultsViewState(parcel);
                    }

                    @Override
                    public SearchResultsViewState[] newArray(int sz) {
                        return new SearchResultsViewState[sz];
                    }
                };

        public int taskCount;
        public List<Tag> tags;

        public SearchResultsViewState(int taskCount, List<Tag> tags) {
            this.taskCount = taskCount;
            this.tags = tags;
        }

        private SearchResultsViewState() {
        }

        private SearchResultsViewState(Parcel source) {
            taskCount = source.readInt();
            int tagsCount = source.readInt();
            if (tagsCount > 0) {
                Parcelable[] array = source.readParcelableArray(FilterView.class.getClassLoader());
                tags = Lists.newArrayList(Iterators.transform(
                        Iterators.forArray(array),
                        (input) -> (Tag) input));
            }
        }

        public SearchResultsViewState copy() {
            SearchResultsViewState searchResultsViewState = new SearchResultsViewState();

            searchResultsViewState.taskCount = taskCount;
            searchResultsViewState.tags = Lists.newArrayList(tags == null ? Collections.emptyList() : tags);

            return searchResultsViewState;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(taskCount);
            int tagsCount = (tags == null) ? 0 : tags.size();
            parcel.writeInt(tagsCount);
            if (tagsCount > 0) {
                Tag[] array = tags.toArray(new Tag[tagsCount]);
                parcel.writeParcelableArray(array, 0);
            }
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
                getQuantityString(R.plurals.task_count, mSearchResultsViewState.taskCount), mSearchResultsViewState.taskCount));

        if (mSearchResultsViewState.tags != null && !mSearchResultsViewState.tags.isEmpty()) {
            final String tags = Joiner.on(", ").join(Iterables.transform(mSearchResultsViewState.tags, Tag::getName));
            stringBuilder.append(String.format(String.format(mContext.getResources().
                    getQuantityString(R.plurals.tags_count, mSearchResultsViewState.tags.size()), tags)));
        }

        mSearchResultsTextView.setText(stringBuilder.toString());
    }
}
