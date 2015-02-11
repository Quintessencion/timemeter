package com.simbirsoft.timemeter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ArrayRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.handlers.JobEventDispatcher;
import com.be.android.library.worker.models.LoadJobResult;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.events.FilterViewStateChangeEvent;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTagListJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.model.Period;
import com.simbirsoft.timemeter.ui.util.TagViewUtils;
import com.squareup.otto.Bus;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.tokenautocomplete.TokenCompleteTextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

@EViewGroup(R.layout.view_filter)
public class FilterView extends FrameLayout implements
        TokenCompleteTextView.TokenListener, DatePeriodView.DatePeriodViewListener {

    public interface OnSelectDateClickListener {
        void onSelectDateClicked();
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

        private FilterState() {
        }

        public FilterState copy() {
            FilterState state = new FilterState();

            state.dateMillis = dateMillis;
            state.periodMillis = periodMillis;
            state.period = period;
            state.tags = Lists.newArrayList(tags);

            return state;
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
            dateMillis = source.readLong();
            periodMillis = source.readLong();
            period = Period.valueOf(source.readString());
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
            parcel.writeString(period.name());
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

    @ViewById(R.id.chooseDateView)
    View mChooseDateView;

    @ViewById(R.id.datePanel)
    ViewGroup mDatePanel;

    @Inject
    Bus mBus;

    private FilterState mState;
    private DatePeriodView mDatePeriodView;
    private FilteredArrayAdapter<Tag> mAdapter;
    private JobEventDispatcher mJobEventDispatcher;
    private TokenCompleteTextView.TokenListener mTokenListener;
    private OnSelectDateClickListener mOnSelectDateClickListener;


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
        if (mOnSelectDateClickListener != null) {
            mOnSelectDateClickListener.onSelectDateClicked();
        }
    }

    @Override
    public void onDateReset() {
        hideDatePeriod();
    }

    @Override
    public void onPeriodSelected(Period period) {
        mState.period = period;
        mBus.post(new FilterViewStateChangeEvent(getViewFilterState()));
    }

    public void setDate(long dateMillis) {
        mState.dateMillis = dateMillis;

        displayDatePeriod();
        mBus.post(new FilterViewStateChangeEvent(getViewFilterState()));
    }

    private void displayDatePeriod() {
        if (mDatePeriodView == null) {
            mDatePeriodView = (DatePeriodView) LayoutInflater.from(getContext())
                    .inflate(R.layout.view_date_period_composed, mDatePanel, false);

            mDatePanel.removeView(mChooseDateView);
            mDatePanel.addView(mDatePeriodView);
        }

        mDatePeriodView.setDateMillis(mState.dateMillis);
        mDatePeriodView.setDatePeriodViewListener(this);
    }

    private void hideDatePeriod() {
        mDatePanel.removeView(mDatePeriodView);
        mDatePanel.addView(mChooseDateView);
        mDatePeriodView = null;
        mBus.post(new FilterViewStateChangeEvent(getViewFilterState()));
    }

    @Click(R.id.chooseDateView)
    void onChooseDateClicked() {
        if (mOnSelectDateClickListener != null) {
            mOnSelectDateClickListener.onSelectDateClicked();
        }
    }

    @AfterViews
    void initializeView() {
        Injection.sUiComponent.injectFilterView(this);

        if (mState == null) {
            mState = new FilterState();
        }

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
        updateFilterState();

        return mState.copy();
    }

    private void updateFilterState() {
        mState.tags = Lists.newArrayList(
                Iterables.transform(mTagsView.getObjects(), input -> (Tag) input));
        if (mDatePeriodView != null) {
            mState.period = mDatePeriodView.getPeriod();
            mState.dateMillis = mDatePeriodView.getDateMillis();
        } else {
            mState.period = null;
            mState.dateMillis = 0;
        }
    }
}
