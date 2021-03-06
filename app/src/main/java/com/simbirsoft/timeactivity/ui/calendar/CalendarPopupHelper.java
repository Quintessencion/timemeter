package com.simbirsoft.timeactivity.ui.calendar;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.handlers.JobEventDispatcher;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.db.model.TaskTimeSpan;
import com.simbirsoft.timeactivity.injection.Injection;
import com.simbirsoft.timeactivity.jobs.LoadTasksForTimespansJob;
import com.simbirsoft.timeactivity.ui.model.TaskBundle;
import com.simbirsoft.timeactivity.ui.views.CalendarPopupView;

import java.util.List;

public class CalendarPopupHelper {
    private static final String LOADER_TAG = "CalendarPopup_loader";
    private static final int POPUP_MARGIN_DEFAULT_DIP = 2;

    private int[] mPopupCoords = new int[2];
    private int[] mPopupDimensions = new int[2];
    private PopupWindow mWindow;

    private Context mContext;
    private CalendarPopupView mView;
    private CalendarPopupAdapter mAdapter;

    private JobEventDispatcher mJobEventDispatcher;
    private View mAnchorView;
    private Point mAnchorPoint;
    private int mPopupMargin;

    public CalendarPopupHelper(Context context) {
        mContext = context;
        mWindow = new PopupWindow(context);
        mWindow.setTouchable(true);
        mWindow.setFocusable(true);
        mWindow.setOutsideTouchable(true);
        mWindow.setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));

        LayoutInflater layoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mView = (CalendarPopupView)layoutInflater.inflate(R.layout.view_calendar_popup_layout, null);
        mWindow.setContentView(mView);
        RecyclerView.LayoutManager layoutManager = new CalendarPopupLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        mView.getRecyclerView().setLayoutManager(layoutManager);
        mAdapter = new CalendarPopupAdapter(context);
        mView.getRecyclerView().setAdapter(mAdapter);

        mAnchorPoint = new Point();
        mPopupMargin =  (int) (mContext.getResources().getDisplayMetrics().density * POPUP_MARGIN_DEFAULT_DIP);

        mJobEventDispatcher = new JobEventDispatcher(mContext);
        mJobEventDispatcher.register(this);
    }

    public void unregister() {
        mJobEventDispatcher.unregister(this);
    }

    @OnJobSuccess(LoadTasksForTimespansJob.class)
    public void onTaskLoaded(LoadJobResult<List<TaskBundle>> result) {
        mAdapter.setItems(result.getData());
        calculatePopupPositionAndSize(mPopupCoords, mPopupDimensions);
        mWindow.setWidth(mPopupDimensions[0]);
        mWindow.setHeight(mPopupDimensions[1]);
        mWindow.showAtLocation(mAnchorView, Gravity.NO_GRAVITY, mPopupCoords[0], mPopupCoords[1]);
    }

    @OnJobFailure(LoadTasksForTimespansJob.class)
    public void onTaskLoadFailed() {
        Toast.makeText(mContext, R.string.error_unable_to_load_task_list, Toast.LENGTH_LONG).show();
    }

    public void show(View anchor, Point anchorPoint, List<TaskTimeSpan> spans) {
        mAnchorView = anchor;
        mAnchorPoint.x = Math.min(mAnchorView.getWidth() - mPopupMargin, Math.max(mPopupMargin, anchorPoint.x));
        mAnchorPoint.y = Math.min(mAnchorView.getHeight() - mPopupMargin, Math.max(mPopupMargin, anchorPoint.y));
        LoadTasksForTimespansJob job = Injection.sJobsComponent.loadTasksJob();
        job.setGroupId(JobManager.JOB_GROUP_UNIQUE);
        job.addTag(LOADER_TAG);
        job.setSpans(spans);
        mJobEventDispatcher.submitJob(job);
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        mWindow.setOnDismissListener(listener);
    }

    public void setTaskClickListener(CalendarPopupAdapter.TaskClickListener listener) {
        mAdapter.setTaskClickListener(listener);
    }

    public void dismiss() {
        mWindow.dismiss();
    }

    public boolean isVisible() {
        return mWindow.isShowing();
    }

    public void updateTask(TaskBundle bundle) {
        mAdapter.replaceItem(bundle);
        calculatePopupPositionAndSize(mPopupCoords, mPopupDimensions);
        mWindow.update(mPopupCoords[0], mPopupCoords[1], mPopupDimensions[0], mPopupDimensions[1]);
    }

    private void calculatePopupPositionAndSize(int[] coords, int[] dimensions) {
        mAnchorView.getLocationOnScreen(coords);
        final int anchorWidth = mAnchorView.getWidth();
        final int anchorHeight = mAnchorView.getHeight();

        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        ((CalendarPopupLayoutManager)mView.getRecyclerView().getLayoutManager()).setMaxWidth(anchorWidth - 2 * mPopupMargin);
        mView.getRecyclerView().measure(measureSpec, measureSpec);
        mView.getUpArrowImage().measure(measureSpec, measureSpec);
        mView.getLeftArrowImage().measure(measureSpec, measureSpec);

        int popupHeight =  Math.min(mView.getRecyclerView().getMeasuredHeight(), anchorHeight - 2 * mPopupMargin);
        int popupWidth = mView.getRecyclerView().getMeasuredWidth();

        int hArrowWidth = mView.getLeftArrowImage().getMeasuredWidth();
        int hArrowHeight = mView.getLeftArrowImage().getMeasuredHeight();
        int vArrowHeight = mView.getUpArrowImage().getMeasuredHeight();
        int vArrowWidth = mView.getUpArrowImage().getMeasuredWidth();

        int xPos, yPos;
        if (popupWidth + hArrowWidth <= getMaxSize(mAnchorPoint.x, anchorWidth)) {
            popupWidth += hArrowWidth;
            xPos = getPositionOnSide(mAnchorPoint.x, anchorWidth, popupWidth);
            yPos = getCenteredPosition(mAnchorPoint.y, anchorHeight, popupHeight);
            ImageView arrowImage = (xPos == mAnchorPoint.x) ? mView.getLeftArrowImage() : mView.getRightArrowImage();
            showArrow(arrowImage);
            ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) arrowImage
                    .getLayoutParams();
            param.topMargin = getArrowMargin(hArrowHeight, mAnchorPoint.y, yPos);
        } else {
            popupHeight = Math.min(popupHeight + vArrowHeight, getMaxSize(mAnchorPoint.y, anchorHeight));
            xPos = getCenteredPosition(mAnchorPoint.x, anchorWidth, popupWidth);
            yPos = getPositionOnSide(mAnchorPoint.y, anchorHeight, popupHeight);
            ImageView arrowImage = (yPos == mAnchorPoint.y) ? mView.getUpArrowImage() : mView.getDownArrowImage();
            showArrow(arrowImage);
            ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) arrowImage
                    .getLayoutParams();
            param.leftMargin = getArrowMargin(vArrowWidth, mAnchorPoint.x, xPos);
        }
        coords[0] += xPos;
        coords[1] += yPos;
        dimensions[0] = popupWidth;
        dimensions[1] = popupHeight;
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

    private void showArrow(ImageView arrowImage) {
        mView.hideAllArrows();
        arrowImage.setVisibility(View.VISIBLE);
    }

    private int getArrowMargin(int arrowSize, int anchorPos, int popupPos) {
        return anchorPos - popupPos - arrowSize / 2;
    }
}
