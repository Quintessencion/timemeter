package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.handlers.JobEventDispatcher;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.events.FilterViewStateChangeEvent;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTagListJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.util.TagViewUtils;
import com.squareup.otto.Bus;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.tokenautocomplete.TokenCompleteTextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

@EViewGroup(R.layout.view_filter)
public class FilterView extends FrameLayout implements TokenCompleteTextView.TokenListener {

    public static class FilterState implements Parcelable {

        public static final Creator<FilterState> CREATOR =
                new Creator<FilterState>() {
                    @Override
                    public FilterState createFromParcel(Parcel parcel) {
                        return new FilterState(parcel);
                    }

                    @Override
                    public FilterState[] newArray(int sz) {
                        return new FilterState[sz];
                    }
                };

        public List<Tag> tags;

        private FilterState() {
        }

        public boolean isEmpty() {
            return tags == null || tags.isEmpty();
        }

        private FilterState(Parcel source) {
            int sz = source.readInt();
            if (sz > 0) {
                Tag[] array = (Tag[]) source.readParcelableArray(FilterView.class.getClassLoader());
                tags = Lists.newArrayList(array);
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            int sz = tags == null ? 0 : tags.size();
            parcel.writeInt(sz);
            if (sz > 0) {
                Tag[] array = tags.toArray(new Tag[sz]);
                parcel.writeParcelableArray(array, 0);
            }
        }
    }

    private static class TagViewHolder {
        ViewGroup itemView;
        TextView tagView;

        TagViewHolder(ViewGroup itemView) {
            this.itemView = itemView;
            tagView = (TextView) itemView.findViewById(android.R.id.title);
        }
    }

    private static final Logger LOG = LogFactory.getLogger(FilterView.class);

    @ViewById(R.id.shadowUp)
    View mShadowUp;

    @ViewById(R.id.shadowDown)
    View mShadowDown;

    @ViewById(R.id.tagsView)
    TagFilterTextView mTagsView;

    @Inject
    Bus mBus;

    private FilteredArrayAdapter<Tag> mAdapter;
    private JobEventDispatcher mJobEventDispatcher;
    private TokenCompleteTextView.TokenListener mTokenListener;


    public FilterView(Context context) {
        super(context);
    }

    public FilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FilterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FilterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TagFilterTextView getTagsView() {
        return mTagsView;
    }

    @AfterViews
    void initializeView() {
        Injection.sUiComponent.injectFilterView(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Hide custom elevation on Lollipop
            mShadowDown.setVisibility(View.GONE);
            mShadowUp.setVisibility(View.GONE);
        }
        final Context context = getContext();
        mJobEventDispatcher = new JobEventDispatcher(context);
        mJobEventDispatcher.register(this);

        mTagsView.allowDuplicates(false);
        mTagsView.allowCollapse(false);
        mTagsView.setImeActionLabel(
                context.getString(R.string.ime_action_done),
                EditorInfo.IME_ACTION_DONE);
        mTagsView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mTagsView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Delete);
        mTagsView.setTokenListener(this);
        mJobEventDispatcher.submitJob(Injection.sJobsComponent.loadTagListJob());
    }

    @OnJobSuccess(LoadTagListJob.class)
    public void onTagListLoaded(LoadJobResult<List<Tag>> result) {
        mAdapter = new FilteredArrayAdapter<Tag>(
                getContext(),
                R.layout.view_tag_filter_popup_list_item,
                result.getData()) {

            @Override
            protected boolean keepObject(Tag tag, String s) {
                final String name = s.trim();

                if (TextUtils.isEmpty(name)) {
                    return false;
                }

                return tag.getName().toLowerCase().contains(s.toLowerCase());
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Tag item = getItem(position);
                TagViewHolder vh;
                if (convertView == null) {
                    convertView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.view_tag_filter_popup_list_item, parent, false);
                    vh = new TagViewHolder((ViewGroup) convertView);
                    convertView.setTag(vh);

                } else {
                    vh = (TagViewHolder) convertView.getTag();
                }

                TagViewUtils.updateTagViewColor(vh.tagView, item.getColor());
                vh.tagView.setText(item.getName());

                return vh.itemView;
            }
        };

        mTagsView.setAdapter(mAdapter);
    }

    public TokenCompleteTextView.TokenListener getTokenListener() {
        return mTokenListener;
    }

    public void setTokenListener(TokenCompleteTextView.TokenListener tokenListener) {
        mTokenListener = tokenListener;
    }

    @OnJobFailure(LoadTagListJob.class)
    public void onTagListLoadFailed() {
        LOG.error("failed to load tag list to tags' view adapter");
    }

    @Override
    protected void onDetachedFromWindow() {
        mJobEventDispatcher.unregister(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onTokenAdded(Object o) {
        if (mAdapter != null && mAdapter.getPosition((Tag) o) < 0) {
            mTagsView.removeObject(o);

        } else if (mTokenListener != null) {
            mTokenListener.onTokenAdded(o);
            mBus.post(new FilterViewStateChangeEvent(getViewFilterState()));
        }
    }

    @Override
    public void onTokenRemoved(Object o) {
        if (mTokenListener == null) {
            return;
        }

        if (mAdapter != null && mAdapter.getPosition((Tag) o) > -1) {
            mTokenListener.onTokenRemoved(o);
            mBus.post(new FilterViewStateChangeEvent(getViewFilterState()));
        }
    }

    public FilterState getViewFilterState() {
        FilterState filter = new FilterState();
        filter.tags = Lists.newArrayList(
                Iterables.transform(mTagsView.getObjects(), input -> (Tag) input));

        return filter;
    }
}
