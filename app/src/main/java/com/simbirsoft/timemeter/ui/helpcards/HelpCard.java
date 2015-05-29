package com.simbirsoft.timemeter.ui.helpcards;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.simbirsoft.timemeter.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.view_help_card)
public class HelpCard extends CardView {

    public interface DataSource {
        int getItemsCount();
        String getMessage(int index);
        String getTitleForActionButton(int index);
        String getTitleForNextButton(int index);
        String getTitleForBackButton(int index);
    }

    @ViewById(R.id.helpCardText)
    protected TextView mTextView;

    @ViewById(R.id.helpCardActionButton)
    protected Button mActionButton;

    @ViewById(R.id.helpCardNextButton)
    protected Button mNextButton;

    @ViewById(R.id.helpCardBackButton)
    protected Button mBackButton;

    private int mPosition = 0;
    private DataSource mDataSource;
    private OnClickListener mOnNextClickListener;
    private OnClickListener mOnBackClickListener;

    public HelpCard(Context context) {
        super(context);
    }

    public HelpCard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HelpCard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        mNextButton.setOnClickListener(v -> {
            if (mOnNextClickListener != null) {
                mOnNextClickListener.onClick(v);
            }
            goToNextItem();
        });
        mBackButton.setOnClickListener(v -> {
            if (mOnBackClickListener != null) {
                mOnBackClickListener.onClick(v);
            }
            goToPrevItem();
        });
        super.onFinishInflate();
    }

    private void goToNextItem() {
        final int nextPosition = mPosition + 1;
        final int itemsCount = mDataSource.getItemsCount();
        if (nextPosition < itemsCount) {
            mPosition = nextPosition;
            presentItemAtIndex(mPosition);
        }
    }

    private void goToPrevItem() {
        final int nextPosition = mPosition - 1;
        if (nextPosition >= 0) {
            mPosition = nextPosition;
            presentItemAtIndex(mPosition);
        }
    }

    public void setOnActionClickListener(View.OnClickListener listener) {
        mActionButton.setOnClickListener(listener);
    }

    public void setOnNextClickListener(View.OnClickListener listener) {
        mOnNextClickListener = listener;
    }

    public void setOnBackClickListener(View.OnClickListener listener) {
        mOnBackClickListener = listener;
    }

    private void reload() {
        mPosition = 0;

        int count = mDataSource.getItemsCount();
        mBackButton.setVisibility(count > 1 ? VISIBLE : GONE);

        presentItemAtIndex(mPosition);
    }

    private void presentItemAtIndex(int index) {
        mTextView.setText(mDataSource.getMessage(index));
        mActionButton.setText(mDataSource.getTitleForActionButton(index));
        mNextButton.setText(mDataSource.getTitleForNextButton(index));
        mBackButton.setText(mDataSource.getTitleForBackButton(index));

        mBackButton.setEnabled(index > 0);
    }

    public void setDataSource(DataSource dataSource) {
        mDataSource = dataSource;
        reload();
    }

    public int getPosition() {
        return mPosition;
    }

    public boolean isLastItemPresented() {
        return mPosition == (mDataSource.getItemsCount() - 1);
    }
}
