package com.simbirsoft.timemeter.ui.activities;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;

import java.util.List;

public class TaskTimeSpanActions implements ActionMode.Callback {

    public interface OnActionListener {
        void onAction(TaskTimeSpanActions sender);
    }

    private FragmentActivity mActivityContext;
    private TaskActivitiesAdapter mAdapter;
    private ActionMode mActionMode;
    private DataObserver mDataObserver;

    private OnActionListener mOnEditListener;
    private OnActionListener mOnRemoveListener;
    private OnActionListener mOnActivated;
    private OnActionListener mOnDeactivated;

    public TaskTimeSpanActions(FragmentActivity activityContext, TaskActivitiesAdapter adapter) {
        mActivityContext = activityContext;
        mAdapter = adapter;
        mDataObserver = new DataObserver();
        mAdapter.registerAdapterDataObserver(mDataObserver);
    }

    public void setOnEditListener(OnActionListener onEditListener) {
        mOnEditListener = onEditListener;
    }

    public void setOnRemoveListener(OnActionListener onRemoveListener) {
        mOnRemoveListener = onRemoveListener;
    }

    public void setOnActivatedListener(OnActionListener listener) {
        mOnActivated = listener;
    }

    public void setOnDeactivatedListener(OnActionListener listener) {
        mOnDeactivated = listener;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.task_activities_context_menu, menu);
        updateActionBarMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                fireActionCallback(mOnEditListener);
                return true;

            case R.id.remove:
                fireActionCallback(mOnRemoveListener);
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        mAdapter.clearSelection();
        fireActionCallback(mOnDeactivated);
    }

    private void updateActionBarMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.edit);
        if (item == null) {
            return;
        }
        item.setVisible(mAdapter.getSelectedSpans().size() == 1);
    }

    private void fireActionCallback(OnActionListener listener) {
        if (listener != null) {
            listener.onAction(this);
        }
    }

    private void updateActionBar() {
        List<TaskTimeSpan> selected = mAdapter.getSelectedSpans();
        if (selected.isEmpty()) {
            if (mActionMode != null) {
                mActionMode.finish();
            }
        } else {
            if(mActionMode == null) {
                mActionMode = mActivityContext.startActionMode(this);
                fireActionCallback(mOnActivated);
            } else {
                updateActionBarMenu(mActionMode.getMenu());
            }
        }
    }

    private class DataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            updateActionBar();
        }
    }
}
