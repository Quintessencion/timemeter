package com.simbirsoft.timeactivity.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.base.JobEvent;
import com.be.android.library.worker.base.ThrottleJob;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.handlers.JobEventDispatcher;
import com.be.android.library.worker.jobs.CallableForkJoinJob;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.db.model.Tag;
import com.simbirsoft.timeactivity.events.FilterViewStateChangeEvent;
import com.simbirsoft.timeactivity.injection.Injection;
import com.simbirsoft.timeactivity.jobs.LoadTagListJob;
import com.simbirsoft.timeactivity.log.LogFactory;
import com.simbirsoft.timeactivity.model.Period;
import com.simbirsoft.timeactivity.ui.util.KeyboardUtils;
import com.simbirsoft.timeactivity.ui.util.ToastUtils;
import com.squareup.otto.Bus;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.tokenautocomplete.TokenCompleteTextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.slf4j.Logger;

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

@EViewGroup(R.layout.view_filter)
public class FilterView extends FrameLayout implements
        TokenCompleteTextView.TokenListener,
        DatePeriodView.DatePeriodViewListener,
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    public interface OnSelectDateClickListener {
        void onSelectDateClicked(Calendar selectedDate);
    }

    private static class SavedState extends BaseSavedState {

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        FilterState mFilterState;

        public SavedState(Parcel source) {
            super(source);

            mFilterState = source.readParcelable(SavedState.class.getClassLoader());
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeParcelable(mFilterState, 0);
        }
    }

    public static class FilterState implements Parcelable {

        public static final long PERIOD_MILLIS_DEFAULT = TimeUnit.DAYS.toMillis(1);

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

        public long dateMillis;
        public long periodMillis;
        public List<Tag> tags;
        public Period period;
        public String searchText;

        private FilterState() {
        }

        public FilterState copy() {
            FilterState state = new FilterState();

            state.dateMillis = dateMillis;
            state.periodMillis = periodMillis;
            state.period = period;
            state.tags = Lists.newArrayList(tags == null ? Collections.emptyList() : tags);
            state.searchText = searchText;

            return state;
        }

        public boolean isEmpty() {
            return hasEmptyTagsAndDate()
                    && TextUtils.isEmpty(searchText);
        }

        public boolean isFilteredBySearchText() {
            return hasEmptyTagsAndDate()
                    && !TextUtils.isEmpty(searchText);
        }

        private boolean hasEmptyTagsAndDate()
        {
            return (tags == null || tags.isEmpty())
                    && dateMillis == 0
                    && period == null;
        }

        private FilterState(Parcel source) {
            int sz = source.readInt();
            if (sz > 0) {
                Parcelable[] array = source.readParcelableArray(FilterView.class.getClassLoader());
                tags = Lists.newArrayList(Iterators.transform(
                        Iterators.forArray(array),
                        (input) -> (Tag) input));
            }
            dateMillis = source.readLong();
            periodMillis = source.readLong();

            if (source.readByte() == 1) {
                period = Period.valueOf(source.readString());
            }
            searchText = source.readString();
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
            parcel.writeLong(dateMillis);
            parcel.writeLong(periodMillis);
            parcel.writeByte((byte) (period == null ? 0 : 1));
            if (period != null) {
                parcel.writeString(period.name());
            }
            parcel.writeString(searchText);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FilterState that = (FilterState) o;

            if (dateMillis != that.dateMillis) return false;
            if (periodMillis != that.periodMillis) return false;
            if (period != that.period) return false;
            if (searchText != null ? !searchText.equals(that.searchText) : that.searchText != null)
                return false;
            if (tags != null ? !tags.equals(that.tags) : that.tags != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (dateMillis ^ (dateMillis >>> 32));
            result = 31 * result + (int) (periodMillis ^ (periodMillis >>> 32));
            result = 31 * result + (tags != null ? tags.hashCode() : 0);
            result = 31 * result + (period != null ? period.hashCode() : 0);
            result = 31 * result + (searchText != null ? searchText.hashCode() : 0);
            return result;
        }
    }

    private static class TagViewHolder {
        ViewGroup itemView;
        TagView tagView;

        TagViewHolder(ViewGroup itemView) {
            this.itemView = itemView;
            tagView = (TagView) itemView.findViewById(android.R.id.title);
        }
    }

    private static final Logger LOG = LogFactory.getLogger(FilterView.class);

    @ViewById(R.id.shadowUp)
    View mShadowUp;

    @ViewById(R.id.shadowDown)
    View mShadowDown;

    @ViewById(R.id.tagsView)
    TagFilterTextView mTagsView;

    @ViewById(R.id.chooseDateView)
    View mChooseDateView;

    @ViewById(R.id.datePanel)
    ViewGroup mDatePanel;

    @StringRes(R.string.hint_reset_filter)
    String mHintResetFilter;

    @Inject
    Bus mBus;

    private final Handler mHandler = new Handler();
    private FilterState mState;
    private DatePeriodView mDatePeriodView;
    private FilteredArrayAdapter<Tag> mAdapter;
    private JobEventDispatcher mJobEventDispatcher;
    private TokenCompleteTextView.TokenListener mTokenListener;
    private OnSelectDateClickListener mOnSelectDateClickListener;
    private boolean mIsSilentUpdate;
    private boolean mIsReset;
    private SearchView mSearchView;
    private ThrottleJob mThrottleJob;
    private LinkedList<Tag> mTags = Lists.newLinkedList();

    /**
     * Used to ignore {@link #onTokenAdded(Object)} events when view state is being restored
     * to prevent emit of multiple filter state change events
     */
    private Set<Tag> mIgnoredEventTokens;

    {
        mIgnoredEventTokens = Sets.newHashSet();
    }

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

    @Override
    public void onDateTextClicked() {
        sendSelectDateClickEvent();
    }

    @Override
    public void onDateReset() {
        hideDatePeriod();
    }

    @Override
    public void onPeriodSelected(Period period) {
        mState.period = period;
        postFilterUpdate();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        mState.searchText = s;
        postFilterUpdate();
        KeyboardUtils.hideSoftInput(mSearchView.getContext(), mSearchView.getWindowToken());

        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        boolean hasSearchableText = "".equals(s.trim()) && !"".equals(s);
        if (!hasSearchableText) {
            mState.searchText = s;

            if (mThrottleJob == null || mThrottleJob.isFinished()) {
                mThrottleJob = new ThrottleJob();
                mThrottleJob.setTargetJob(new CallableForkJoinJob(
                        JobManager.JOB_GROUP_DEFAULT, () -> {

                    mHandler.post(FilterView.this::postFilterUpdate);

                    return JobEvent.ok();
                }));
                mThrottleJob.getThrottle().updateTimeout(180);
                JobManager.getInstance().submitJob(mThrottleJob);
            }

            mThrottleJob.getThrottle().updateTimeout(180);
        }

        return true;
    }

    public void setDate(long dateMillis) {
        mState.dateMillis = dateMillis;

        displayDatePeriod();
        postFilterUpdate();
    }

    public SearchView getSearchView() {
        return mSearchView;
    }

    public void setSearchView(SearchView searchView) {
        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(null);
        }
        mSearchView = searchView;
        if (mState != null) {
            mSearchView.setQuery(mState.searchText, false);

            // Force to not re-open soft keyboard
            mSearchView.post(() -> KeyboardUtils.hideSoftInput(
                    mSearchView.getContext(), mSearchView.getWindowToken()));
        }

        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
    }

    @Override
    public boolean onClose() {
        mState.searchText = null;
        postFilterUpdate();
        return false;
    }

    private void displayDatePeriod() {
        if (mDatePeriodView == null) {
            mDatePeriodView = (DatePeriodView) LayoutInflater.from(getContext())
                    .inflate(R.layout.view_activities_date_period_composed, mDatePanel, false);

            mDatePanel.removeView(mChooseDateView);
            mDatePanel.addView(mDatePeriodView);
        }

        mDatePeriodView.setStartDateMillis(mState.dateMillis);
        mDatePeriodView.setDatePeriodViewListener(this);
    }

    private void hideDatePeriod() {
        if (mDatePeriodView != null) {
            mDatePanel.removeView(mDatePeriodView);
            mDatePanel.addView(mChooseDateView);
        }
        mDatePeriodView = null;
        postFilterUpdate();
    }

    @Click(R.id.chooseDateView)
    void onChooseDateClicked() {
        sendSelectDateClickEvent();
    }

    @Click(R.id.resetFilterView)
    void onResetFilterClicked() {
        mIsSilentUpdate = true;
        mTags.clear();
        for (Object tag : mTagsView.getObjects()) {
            mTagsView.removeObject(tag);
        }
        hideDatePeriod();
        mIsSilentUpdate = false;
        mIsReset = true;
        postFilterUpdate();
        mIsReset = false;
    }

    @LongClick(R.id.resetFilterView)
    void onResetFilterLongClicked(View v) {
        ToastUtils.showToastWithAnchor(getContext(),
                mHintResetFilter, v, Toast.LENGTH_SHORT);
    }

    @AfterViews
    void initializeView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Hide custom elevation on Lollipop
            mShadowDown.setVisibility(View.GONE);
            mShadowUp.setVisibility(View.GONE);
        }

        if (isInEditMode()) {
            return;
        }

        Injection.sUiComponent.injectFilterView(this);

        if (mState == null) {
            mState = new FilterState();
        }
        final Context context = getContext();
        mJobEventDispatcher = new JobEventDispatcher(context);
        mJobEventDispatcher.register(this);

        mTagsView.setVisibilityStateCallback(() -> FilterView.this.getVisibility() == View.VISIBLE);
        mTagsView.allowDuplicates(false);
        mTagsView.allowCollapse(false);
        mTagsView.setImeActionLabel(
                context.getString(R.string.ime_action_done),
                EditorInfo.IME_ACTION_DONE);
        mTagsView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mTagsView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Delete);
        mTagsView.setTokenListener(this);
        mTagsView.setOnTouchListener((v, event) -> {
            mAdapter.getFilter().filter(mTagsView.getCurrentCompletionText());
            mTagsView.showDropDown();
            return false;
        });
        mJobEventDispatcher.submitJob(Injection.sJobsComponent.loadTagListJob());
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.mFilterState = mState;

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mState = ss.mFilterState;

        if (mState.dateMillis != 0) {
            displayDatePeriod();
        }
    }

    public OnSelectDateClickListener getOnSelectDateClickListener() {
        return mOnSelectDateClickListener;
    }

    public void setOnSelectDateClickListener(OnSelectDateClickListener onSelectDateClickListener) {
        mOnSelectDateClickListener = onSelectDateClickListener;
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

                if (Iterables.indexOf(
                        mTagsView.getObjects(),
                        (token) -> ((Tag) token).getName().equalsIgnoreCase(tag.getName())) > -1) {

                    return false;

                } else if (TagFilterTextView.COMPLETION_TEXT_ALLOW_ANY.equals(s)) {
                    return true;
                }

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

                vh.tagView.setTag(item);
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
        if (mJobEventDispatcher != null) {
            mJobEventDispatcher.unregister(this);
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void onTokenAdded(Object o) {
        if (mAdapter == null) {
            return;
        }

        final Tag tag = (Tag) o;

        if (mAdapter.getPosition(tag) < 0) {
            // Allow to add only existing tags
            mTagsView.removeObject(tag);
            mTags.remove(tag);

        } else {
            if (!mTags.contains(tag)) {
                mTags.add(tag);
            }
            if (mTokenListener != null) {
                mTokenListener.onTokenAdded(tag);
            }
            KeyboardUtils.hideSoftInput(mTagsView.getContext(), mTagsView.getWindowToken());
            postFilterUpdate();
        }

        // Need to manually re-filter tag list adapter
        // to exclude previously added tags from list
        mAdapter.getFilter().filter(mTagsView.getCurrentCompletionText());
        mTagsView.dismissDropDown();
    }

    @Override
    public void onTokenRemoved(Object o) {
        mTags.remove(o);

        if (mAdapter != null) {
            if (mTokenListener != null && mAdapter.getPosition((Tag) o) > -1) {
                mTokenListener.onTokenRemoved(o);
            }
            // TagFilterTextView automatically re-filter it's adapter after tag remove
        }

        postFilterUpdate();
        mTagsView.dismissDropDown();
    }

    public void setFilterState(FilterState state) {
        mState = state.copy();

        mIsSilentUpdate = true;
        mTags.clear();
        if (state.tags != null && !state.tags.isEmpty()) {
            mTags.addAll(state.tags);

            for (Tag tag : state.tags) {
                mIgnoredEventTokens.add(tag);
                mTagsView.addObject(tag);
            }
        } else {
            mTagsView.clear();
            mIgnoredEventTokens.clear();
            mTags.clear();
        }

        if (mState.dateMillis == 0) {
            hideDatePeriod();
        } else {
            displayDatePeriod();
        }

        if (mDatePeriodView != null && mState.period != null) {
            mDatePeriodView.setPeriod(mState.period);
        }

        mIsSilentUpdate = false;
    }

    public FilterState getViewFilterState() {
        updateFilterState();

        return mState.copy();
    }

    public void updateDateView() {
        if (mDatePeriodView != null) {
            mDatePeriodView.updateStartDateView();
            mDatePeriodView.updateEndDateView();
        }
    }

    private void postFilterUpdate() {
        if (mIsSilentUpdate) {
            return;
        }

        final FilterViewStateChangeEvent ev = new FilterViewStateChangeEvent(getViewFilterState());
        ev.setReset(mIsReset);
        mHandler.post(() -> mBus.post(ev));
    }

    private void updateFilterState() {
        if (mDatePeriodView != null) {
            mState.period = mDatePeriodView.getPeriod();
            mState.dateMillis = mDatePeriodView.getStartDateMillis();
        } else {
            mState.period = null;
            mState.dateMillis = 0;
        }

        mState.tags = ImmutableList.copyOf(mTags);
    }

    private void sendSelectDateClickEvent() {
        if (mOnSelectDateClickListener != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(mState.dateMillis == 0 ? System.currentTimeMillis() : mState.dateMillis);
            mOnSelectDateClickListener.onSelectDateClicked(cal);
        }
    }
}
