package com.simbirsoft.timeactivity.ui.calendar;

import android.animation.Animator;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.ScrollView;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.be.android.library.worker.util.JobSelector;
import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.controller.HelpCardController;
import com.simbirsoft.timeactivity.controller.ITaskActivityManager;
import com.simbirsoft.timeactivity.db.model.TaskTimeSpan;
import com.simbirsoft.timeactivity.events.TaskActivityStoppedEvent;
import com.simbirsoft.timeactivity.injection.Injection;
import com.simbirsoft.timeactivity.jobs.LoadActivityCalendarJob;
import com.simbirsoft.timeactivity.ui.base.FragmentContainerActivity;
import com.simbirsoft.timeactivity.ui.main.MainPageFragment;
import com.simbirsoft.timeactivity.ui.main.MainPagerAdapter;
import com.simbirsoft.timeactivity.ui.model.CalendarData;
import com.simbirsoft.timeactivity.ui.model.CalendarPeriod;
import com.simbirsoft.timeactivity.ui.model.TaskBundle;
import com.simbirsoft.timeactivity.ui.taskedit.EditTaskFragment;
import com.simbirsoft.timeactivity.ui.taskedit.ViewTaskFragment;
import com.simbirsoft.timeactivity.ui.taskedit.ViewTaskFragment_;
import com.simbirsoft.timeactivity.ui.views.CalendarNavigationView;
import com.simbirsoft.timeactivity.ui.views.CalendarViewPager;
import com.simbirsoft.timeactivity.ui.helpcards.HelpCard;
import com.simbirsoft.timeactivity.ui.helpcards.HelpCardPresenter;
import com.simbirsoft.timeactivity.ui.helpcards.HelpCardSource;
import com.simbirsoft.timeactivity.ui.views.WeekCalendarView;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

@EFragment(R.layout.fragment_activity_calendar)
public class ActivityCalendarFragment extends MainPageFragment implements MainPagerAdapter.PageTitleProvider,
        JobLoader.JobLoaderCallbacks, CalendarNavigationView.OnCalendarNavigateListener,
        WeekCalendarView.OnCellClickListener, PopupWindow.OnDismissListener,
        CalendarPopupAdapter.TaskClickListener, HelpCardPresenter {

    private static final String CALENDAR_LOADER_TAG = "ActivityCalendarFragment_calendar_loader";

    @ViewById(R.id.calendarScrollView)
    ScrollView mCalendarScrollView;

    @ViewById(R.id.calendarViewPager)
    CalendarViewPager mCalendarViewPager;

    @ViewById(R.id.calendarNavigationView)
    CalendarNavigationView mCalendarNavigationView;

    @ViewById(R.id.helpCard)
    protected HelpCard mHelpCard;

    @InstanceState
    CalendarPeriod mCalendarPeriod;


    private CalendarPagerAdapter mPagerAdapter;
    private CalendarPopupHelper mPopupHelper;
    private long mScrollViewMillisOffset = -1;
    boolean mHelpCardSettedUp;
    private ITaskActivityManager mTaskActivityManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injection.sUiComponent.injectActivityCalendarFragment(this);
        mTaskActivityManager = Injection.sTaskManager.taskActivityManager();
    }

    @AfterViews
    void bindViews() {
        mPopupHelper = new CalendarPopupHelper(getActivity());
        mPopupHelper.setOnDismissListener(this);
        mPopupHelper.setTaskClickListener(this);
        mPagerAdapter = new CalendarPagerAdapter(getActivity(), mCalendarViewPager, this);
        mCalendarNavigationView.setOnCalendarNavigateListener(this);

        requestLoad(CALENDAR_LOADER_TAG, this);

        getBus().register(this);
    }

    @Override
    public void onDestroyView() {
        mPopupHelper.unregister();
        getBus().unregister(this);
        super.onDestroyView();
    }

    @Override
    public String getPageTitle(Resources resources) {
        return resources.getString(R.string.title_activity_calendar);
    }

    @Override
    protected void onFilterViewStateChanged() {
        JobManager.getInstance().cancelAll(JobSelector.forJobTags(CALENDAR_LOADER_TAG));
        saveScrollViewOffset();
        String loaderTag = getFilterLoaderTag(CALENDAR_LOADER_TAG);
        requestLoad(loaderTag, this);
    }

    @Override
    public void onPageSelected() {
        if (mTaskActivityManager.hasActiveTask()) {
            setContentInvalidated(true);
        }
        super.onPageSelected();
    }

    @OnJobSuccess(LoadActivityCalendarJob.class)
    public void onCalendarActivityLoaded(LoadJobResult<CalendarData> result) {
        mCalendarPeriod = result.getData().getCalendarPeriod();
        mCalendarNavigationView.setCalendarPeriod(mCalendarPeriod);
        mPagerAdapter.setCurrentViewActivityCalendar(result.getData().getActivityCalendar());
        adjustScrollViewOffset();
    }

    @OnJobFailure(LoadActivityCalendarJob.class)
    public void onCalendarActivityLoadFailed() {
       showToast(R.string.error_unable_to_load_calendar_data);
    }

    @Override
    public Job onCreateJob(String s) {
        LoadActivityCalendarJob job = Injection.sJobsComponent.loadActivityCalendarJob();
        job.setGroupId(JobManager.JOB_GROUP_UNIQUE);

        if (mCalendarPeriod != null) {
            job.setPrevFilterDateMillis(mCalendarPeriod.getFilterDateMillis());
            job.setStartDate(mCalendarPeriod.getStartDate());
            job.setEndDate(mCalendarPeriod.getEndDate());
        }

        fillTaskLoadFilter(job.getTaskLoadFilter());
        job.addTag(CALENDAR_LOADER_TAG);
        return job;
    }

    @Override
    public void onMovedNext(Date newStartDate, Date newEndDate) {
        saveScrollViewOffset();
        mPagerAdapter.moveNext(newStartDate, newEndDate);
        requestLoad(newStartDate, newEndDate);
    }

    @Override
    public void onMovedPrev(Date newStartDate, Date newEndDate) {
        saveScrollViewOffset();
        mPagerAdapter.movePrev(newStartDate, newEndDate);
        requestLoad(newStartDate, newEndDate);
    }

    @Subscribe
    public void onTaskStopped(TaskActivityStoppedEvent event) {
        setContentInvalidated(true);
    }

    private void saveScrollViewOffset() {
        WeekCalendarView v = mPagerAdapter.getCurrentView();
        mScrollViewMillisOffset = (v != null) ? v.getMillisOffset(mCalendarScrollView.getScrollY()) : -1;
    }

    private void adjustScrollViewOffset() {
        WeekCalendarView v = mPagerAdapter.getCurrentView();
        if (mScrollViewMillisOffset < 0 || v == null) {
            return;
        }
        int offset = v.getPixelOffset(mScrollViewMillisOffset);
        if (mCalendarScrollView.getScrollY() != offset) {
            mCalendarScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mCalendarScrollView.scrollTo(0, offset);
                }
            });
        }
    }

    private void requestLoad(Date newStartDate, Date newEndDate) {
        JobManager.getInstance().cancelAll(JobSelector.forJobTags(CALENDAR_LOADER_TAG));

        String loaderTag = CALENDAR_LOADER_TAG
                + "period:"
                + String.valueOf(newStartDate.getTime())
                + "_"
                + String.valueOf(newEndDate.getTime());
        requestLoad(loaderTag, this);
    }

    @Override
    public void onCellClicked(Point point, List<TaskTimeSpan> spans) {
        point.offset(0, -mCalendarScrollView.getScrollY());
        mPopupHelper.show(mCalendarScrollView, point, spans);
    }

    @Override
    public void onDismiss() {
        mPagerAdapter.deselectCurrentViewCell();
    }

    @Override
    public void onTaskClicked(TaskBundle item) {
        Bundle args = new Bundle();
        args.putParcelable(ViewTaskFragment.EXTRA_TASK_BUNDLE, item);

        Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                getActivity(), ViewTaskFragment_.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, REQUEST_CODE_PROCESS_TASK);
    }

    @Override
    protected void reloadContent() {
        super.reloadContent();
        requestReload(CALENDAR_LOADER_TAG, this);
    }

    @Override
    protected void onTaskUpdated(Intent data) {
        super.onTaskUpdated(data);
        if (mPopupHelper.isVisible()) {
            mPopupHelper.updateTask(data.getParcelableExtra(
                    EditTaskFragment.EXTRA_TASK_BUNDLE));
        }
    }

    @Override
    protected void onTaskRemoved(Intent data) {
        if (isSelected()) {
            if (mPopupHelper.isVisible()) {
                mPopupHelper.dismiss();
            }
            mPagerAdapter.removeSpansFromCurrentView(data.getLongExtra(
                    EditTaskFragment.EXTRA_TASK_ID, -1));
            showTaskRemoveUndoBar(data.getParcelableExtra(
                    EditTaskFragment.EXTRA_TASK_BUNDLE));

        } else {
            invalidateContent();
        }
    }

    @Override
    protected Logger createLogger() {
        return LoggerFactory.getLogger(ActivityCalendarFragment.class);
    }

    @Override
    protected int getHelpCardToPresent(HelpCardController controller) {
        if (!controller.isPresented(HelpCardController.HELP_CARD_CALENDAR)) {
            return HelpCardController.HELP_CARD_CALENDAR;
        }
        return super.getHelpCardToPresent(controller);
    }

    @Override
    protected HelpCardPresenter getHelpCardPresenter() {
        return this;
    }

    @Override
    public void show() {
        if (!mHelpCardSettedUp) {
            setupHelpCard(mHelpCard);
            mHelpCardSettedUp = true;
        }

        mHelpCard.setVisibility(View.VISIBLE);
        mHelpCard.setScaleX(0.0F);
        mHelpCard.setScaleY(0.0F);
        mHelpCard.animate().scaleX(1.0F).scaleY(1.0F).setDuration(250);
    }

    @Override
    public void hide() {
        mHelpCard.animate().scaleX(0.0F).scaleY(0.0F).setDuration(250).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mHelpCard.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }

    @Override
    public void setHelpCardSource(HelpCardSource user) {
        // do nothing because this fragment is help card user
    }
}
