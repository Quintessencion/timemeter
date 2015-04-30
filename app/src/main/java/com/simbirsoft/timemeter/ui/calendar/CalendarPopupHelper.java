package com.simbirsoft.timemeter.ui.calendar;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.handlers.JobEventDispatcher;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Task;
import com.simbirsoft.timemeter.db.model.TaskTimeSpan;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTasksJob;
import com.simbirsoft.timemeter.log.LogFactory;
import com.simbirsoft.timemeter.ui.model.TaskBundle;

import org.slf4j.Logger;

import java.util.List;

public class CalendarPopupHelper {
    private static final Logger LOG = LogFactory.getLogger(CalendarPopupHelper.class);
    private static final String LOADER_TAG = "CalendarPopup_loader";
    private static final int POPUP_MARGIN_DEFAULT_DIP = 2;

    private WindowManager mWindowManager;

    private Context mContext;
    private PopupWindow mWindow;

    private View mView;
    private RecyclerView mRecyclerView;
    private CalendarPopupAdapter mAdapter;

    private Drawable mBackgroundDrawable = null;
    private ShowListener showListener;
    private JobEventDispatcher mJobEventDispatcher;
    private View mAnchorView;
    private Point mAnchorPoint;
    private int mPopupMargin;

    public CalendarPopupHelper(Context context, int viewResource) {
        mContext = context;
        mWindow = new PopupWindow(context);

        mWindowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);


        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setContentView(layoutInflater.inflate(viewResource, null));
        mRecyclerView = (RecyclerView)mView.findViewById(android.R.id.list);
        RecyclerView.LayoutManager layoutManager = new CalendarPopupLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new CalendarPopupAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mPopupMargin =  (int) (context.getResources().getDisplayMetrics().density * POPUP_MARGIN_DEFAULT_DIP);

        mJobEventDispatcher = new JobEventDispatcher(mContext);
        mJobEventDispatcher.register(this);
    }


    public CalendarPopupHelper(Context context) {
        this(context, R.layout.view_calendar_popup);
    }

    public void unregister() {
        mJobEventDispatcher.unregister(this);
    }

    @OnJobSuccess(LoadTasksJob.class)
    public void onTaskLoaded(LoadJobResult<List<TaskBundle>> result) {
        preShow();
        int[] location = new int[2];
        mAnchorView.getLocationOnScreen(location);

        mAdapter.setItems(result.getData());
        mRecyclerView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        int popupHeight = mRecyclerView.getMeasuredHeight() + mView.getPaddingTop() + mView.getPaddingBottom();
        int popupWidth = mRecyclerView.getMeasuredWidth() + mView.getPaddingLeft() + mView.getPaddingRight();

        final int anchorWidth = mAnchorView.getWidth();
        final int anchorHeight = mAnchorView.getHeight();

        popupWidth = Math.min(popupWidth, anchorWidth - 2 * mPopupMargin);
        popupHeight = Math.min(popupHeight, anchorHeight - 2 * mPopupMargin);

        int xPos, yPos;
        if (popupWidth <= getMaxSize(mAnchorPoint.x, anchorWidth)) {
            xPos = getPositionOnSide(mAnchorPoint.x, anchorWidth, popupWidth);
            yPos = getCenteredPosition(mAnchorPoint.y, anchorHeight, popupHeight);
        } else {
            popupHeight = Math.min(popupHeight, getMaxSize(mAnchorPoint.y, anchorHeight));
            xPos = getCenteredPosition(mAnchorPoint.x, anchorWidth, popupWidth);
            yPos = getPositionOnSide(mAnchorPoint.y, anchorHeight, popupHeight);
        }
        mWindow.setWidth(popupWidth);
        mWindow.setHeight(popupHeight);
        mWindow.showAtLocation(mAnchorView, Gravity.NO_GRAVITY, xPos + location[0], yPos + location[1]);
    }

    @OnJobFailure(LoadTasksJob.class)
    public void onTaskLoadFailed() {
        // TODO: display error explanation message
        LOG.error("failed to load tasks");
    }

    public void show(View anchor, Point anchorPoint, List<TaskTimeSpan> spans) {
        mAnchorView = anchor;
        mAnchorPoint = anchorPoint;
        LoadTasksJob job = Injection.sJobsComponent.loadTasksJob();
        job.setGroupId(JobManager.JOB_GROUP_UNIQUE);
        job.addTag(LOADER_TAG);
        job.setSpans(spans);
        mJobEventDispatcher.submitJob(job);
    }


    private void preShow() {
        if (mView == null)
            throw new IllegalStateException("view undefined");

        if (showListener != null) {
            showListener.onPreShow();
            showListener.onShow();
        }

        if (mBackgroundDrawable == null)
            mWindow.setBackgroundDrawable(new BitmapDrawable());
        else
            mWindow.setBackgroundDrawable(mBackgroundDrawable);

        mWindow.setTouchable(true);
        mWindow.setFocusable(true);
        mWindow.setOutsideTouchable(true);

        mWindow.setContentView(mView);

        mAnchorPoint.x = Math.min(mAnchorView.getWidth() - mPopupMargin, Math.max(mPopupMargin, mAnchorPoint.x));
        mAnchorPoint.y = Math.min(mAnchorView.getHeight() - mPopupMargin, Math.max(mPopupMargin, mAnchorPoint.y));
    }

    public void setBackgroundDrawable(Drawable background) {
        mBackgroundDrawable = background;
    }

    public void setContentView(View root) {
        mView = root;

        mWindow.setContentView(root);
    }

    public void setContentView(int layoutResID) {
        LayoutInflater inflator = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setContentView(inflator.inflate(layoutResID, null));
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        mWindow.setOnDismissListener(listener);
    }

    public void dismiss() {
        mWindow.dismiss();
        if (showListener != null) {
            showListener.onDismiss();
        }
    }

    public static interface ShowListener {
        void onPreShow();
        void onDismiss();
        void onShow();
    }

    public void setShowListener(ShowListener showListener) {
        this.showListener = showListener;
    }

    private int getMaxSize(int anchorPos, int anchorSize) {
        return Math.max(anchorPos, anchorSize - anchorPos) - mPopupMargin;
    }

    private int getCenteredPosition(int anchorPos, int anchorSize, int popupSize) {
        int pos = (anchorPos - (popupSize / 2));
        if (pos + popupSize > anchorSize - mPopupMargin) {
            pos = anchorSize - mPopupMargin - popupSize;
        } else if (pos < mPopupMargin) {
            pos = mPopupMargin;
        }
        return pos;
    }

    private int getPositionOnSide(int anchorPos, int anchorSize, int popupSize) {
        return  (anchorPos < anchorSize / 2) ? anchorPos : anchorPos - popupSize;
    }
}
