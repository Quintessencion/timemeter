package com.simbirsoft.timemeter.ui.tags;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.transitions.everywhere.utils.Objects;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.ui.util.TagViewUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class TagListAdapter extends RecyclerView.Adapter<TagListAdapter.ViewHolder>
        implements Filterable {

    private static final int FADE_DURATION_MILLIS = 180;

    public interface ItemClickListener {
        void onItemEditClicked(Tag item);
        void onItemEditLongClicked(Tag item, View itemView);
        void onItemEditColorClicked(Tag item);
        void onItemEditColorLongClicked(Tag item, View itemView);
        void onItemClicked(Tag item);
    }

    public static abstract class AbsItemClickListener implements ItemClickListener {
        @Override
        public void onItemEditClicked(Tag item) {
        }

        @Override
        public void onItemEditLongClicked(Tag item, View itemView) {
        }

        @Override
        public void onItemEditColorClicked(Tag item) {
        }

        @Override
        public void onItemEditColorLongClicked(Tag item, View itemView) {
        }

        @Override
        public void onItemClicked(Tag item) {
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

        FrameLayout tagViewContainer;
        ViewGroup actionPanel;
        TextView tagView;
        View editButtonView;
        View editColorView;
    }

    public class TagFilter extends Filter {

        private final Set<Tag> mExcludeTags = Sets.newHashSet();

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();

            final String filterString = charSequence == null
                    ? null
                    : charSequence.toString().toLowerCase().trim();
            final boolean isEmptyFilter = TextUtils.isEmpty(filterString);

            synchronized (mItemsOriginal) {
                results.values = Lists.newArrayList(Iterables.filter(mItemsOriginal,
                        (input) -> !mExcludeTags.contains(input)
                                && (isEmptyFilter
                                        || input.getName().toLowerCase().contains(filterString))));
            }

            return results;
        }

        public TagFilter excludeTags(Collection<Tag> tags) {
            mExcludeTags.addAll(tags);

            return this;
        }
        public TagFilter clearExclusions() {
            mExcludeTags.clear();

            return this;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mItems.clear();
            mItems.addAll((Collection<Tag>)filterResults.values);
            notifyDataSetChanged();
        }
    }

    private long mToggleActionPanelTimeMillis;
    private boolean mIsActionButtonsShown;
    private final List<Tag> mItemsOriginal;
    private final List<Tag> mItems;
    private ItemClickListener mItemClickListener;
    private TagFilter mFilter;

    @Override
    public TagFilter getFilter() {
        if (mFilter == null) {
            mFilter = new TagFilter();
        }

        return mFilter;
    }

    private final View.OnClickListener mItemViewClickListener =
            (view) -> {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClicked((Tag) view.getTag());
                }
            };

    private final View.OnClickListener mItemEditClickListener =
            (view) -> {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemEditClicked((Tag) view.getTag());
                }
            };

    private final View.OnLongClickListener mItemEditLongClickListener =
            (view) -> {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemEditLongClicked((Tag) view.getTag(), view);

                    return true;
                }

                return false;
            };

    private final View.OnClickListener mItemEditColorCl1ickListener =
            (view) -> {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemEditColorClicked((Tag) view.getTag());
                }
            };

    private final View.OnLongClickListener mItemEditColorLongCl1ickListener =
            (view) -> {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemEditColorLongClicked((Tag) view.getTag(), view);

                    return true;
                }

                return false;
            };

    public TagListAdapter() {
        mItems = Lists.newArrayList();
        mItemsOriginal = Lists.newArrayList();
        setHasStableIds(true);
    }

    public boolean containsItemWithName(String tagName) {
        if (TextUtils.isEmpty(tagName)) {
            return false;
        }

        return findItemWithName(tagName) != null;
    }

    public Tag findItemWithName(String tagName) {
        final String pattern = tagName.trim().toLowerCase();

        int index = Iterables.indexOf(mItemsOriginal, (input) ->
                Objects.equal(input.getName().toLowerCase(), pattern));

        if (index < 0) return null;

        return mItemsOriginal.get(index);
    }

    public void setOriginItems(Collection<Tag> items) {
        mItemsOriginal.clear();
        mItemsOriginal.addAll(items);
    }

    public void setItems(Collection<Tag> items) {
        setOriginItems(items);
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public Tag findItemById(long tagId) {
        int index = Iterables.indexOf(mItems,
                (input) -> Objects.equal(input.getId(), tagId));

        if (index < 0) {
            return null;
        }

        return mItems.get(index);
    }

    public void replaceItem(RecyclerView recyclerView, Tag item) {
        synchronized (mItemsOriginal) {
            replaceItemImpl(mItemsOriginal, recyclerView, item);
            replaceItemImpl(mItems, recyclerView, item);
        }
    }

    private void replaceItemImpl(List<Tag> items, RecyclerView recyclerView, Tag item) {
        int index = Iterables.indexOf(items,
                (input) -> Objects.equal(input.getId(), item.getId()));

        Preconditions.checkElementIndex(index, items.size());
        items.set(index, item);

        ViewHolder vh = (ViewHolder) recyclerView.findViewHolderForItemId(item.getId());
        if (vh == null) {
            return;
        }

        bindViewHolderImpl(vh, item);
    }

    public boolean isActionButtonsShown() {
        return mIsActionButtonsShown;
    }

    public void setActionButtonsShown(boolean isActionButtonsShown) {
        if (mIsActionButtonsShown == isActionButtonsShown) {
            return;
        }

        mToggleActionPanelTimeMillis = System.currentTimeMillis();
        mIsActionButtonsShown = isActionButtonsShown;
        notifyDataSetChanged();
    }

    public ItemClickListener getItemClickListener() {
        return mItemClickListener;
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public Tag getItem(int pos) {
        return mItems.get(pos);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.view_tag_list_item, parent, false);

        int tagColor = view.getResources().getColor(R.color.primaryDark);

        ViewHolder vh = new ViewHolder(view);

        vh.tagViewContainer = (FrameLayout) view.findViewById(R.id.tagViewContainer);
        vh.tagView = TagViewUtils.inflateTagView(inflater, vh.tagViewContainer, tagColor);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        vh.tagView.setLayoutParams(params);
        vh.tagViewContainer.addView(vh.tagView);

        vh.actionPanel = (ViewGroup) view.findViewById(R.id.actionPanel);

        if (mIsActionButtonsShown) {
            vh.actionPanel.setVisibility(View.VISIBLE);
        } else {
            vh.actionPanel.setVisibility(View.GONE);
        }

        vh.editButtonView = vh.actionPanel.findViewById(android.R.id.edit);
        vh.editButtonView.setOnClickListener(mItemEditClickListener);
        vh.editButtonView.setOnLongClickListener(mItemEditLongClickListener);

        vh.editColorView = vh.actionPanel.findViewById(R.id.pickColor);
        vh.editColorView.setOnClickListener(mItemEditColorCl1ickListener);
        vh.editColorView.setOnLongClickListener(mItemEditColorLongCl1ickListener);

        vh.itemView.setOnClickListener(mItemViewClickListener);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Tag item = getItem(position);

        bindViewHolderImpl(holder, item);
    }

    private void bindViewHolderImpl(ViewHolder vh, Tag item) {
        final int toggleDelta = (int) (System.currentTimeMillis() - mToggleActionPanelTimeMillis);

        vh.tagView.setText(item.getName());
        vh.editButtonView.setTag(item);
        vh.editColorView.setTag(item);
        vh.itemView.setTag(item);

        TagViewUtils.updateTagViewColor(vh.tagView, item.getColor());

        if (mIsActionButtonsShown) {
            if (vh.actionPanel.getVisibility() != View.VISIBLE) {
                if (toggleDelta < FADE_DURATION_MILLIS) {
                    Animation anim = AnimationUtils.loadAnimation(
                            vh.actionPanel.getContext(), android.R.anim.fade_in);
                    anim.setDuration(FADE_DURATION_MILLIS);
                    vh.actionPanel.startAnimation(anim);
                }

                vh.actionPanel.setVisibility(View.VISIBLE);
            }
        } else {
            if (vh.actionPanel.getVisibility() != View.GONE) {

                if (toggleDelta < FADE_DURATION_MILLIS) {
                    Animation anim = AnimationUtils.loadAnimation(
                            vh.actionPanel.getContext(), android.R.anim.fade_out);
                    anim.setDuration(FADE_DURATION_MILLIS);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            vh.actionPanel.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    vh.actionPanel.startAnimation(anim);
                } else {
                    vh.actionPanel.setVisibility(View.GONE);
                }
            }
        }
    }
}
