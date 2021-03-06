package com.simbirsoft.timeactivity.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transitions.everywhere.ChangeBounds;
import android.transitions.everywhere.Fade;
import android.transitions.everywhere.TransitionManager;
import android.transitions.everywhere.TransitionSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.controllers.JobManager;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.be.android.library.worker.util.JobSelector;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.google.common.base.Preconditions;
import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.simbirsoft.timeactivity.Consts;
import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.db.model.TaskTimeSpan;
import com.simbirsoft.timeactivity.events.TaskActivityUpdateEvent;
import com.simbirsoft.timeactivity.injection.ApplicationModule;
import com.simbirsoft.timeactivity.injection.Injection;
import com.simbirsoft.timeactivity.jobs.LoadTaskActivitiesJob;
import com.simbirsoft.timeactivity.model.TaskLoadFilter;
import com.simbirsoft.timeactivity.ui.base.BaseFragment;
import com.simbirsoft.timeactivity.ui.base.DialogContainerActivity;
import com.simbirsoft.timeactivity.ui.base.FragmentContainerActivity;
import com.simbirsoft.timeactivity.ui.model.TaskActivityItem;
import com.simbirsoft.timeactivity.ui.stats.StatisticsViewBinder;
import com.simbirsoft.timeactivity.ui.stats.StatsDetailsFragment;
import com.simbirsoft.timeactivity.ui.stats.StatsDetailsFragment_;
import com.simbirsoft.timeactivity.ui.taskedit.ViewTaskFragment;
import com.simbirsoft.timeactivity.ui.util.DeviceUtils;
import com.simbirsoft.timeactivity.ui.views.DatePeriodView;
import com.simbirsoft.timeactivity.ui.views.ProgressLayout;
import com.simbirsoft.timeactivity.ui.views.TaskActivitiesFilterView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

@EFragment(R.layout.fragment_task_activities)
public class TaskActivitiesFragment extends BaseFragment implements
        JobLoader.JobLoaderCallbacks,
        TaskActivitiesFilterView.OnTaskActivitiesFilterListener,
        DatePickerDialog.OnDateSetListener, TaskActivitiesAdapter.OnSelectActiveListener {
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TITLE = "extra_title";

    private static final String LOADER_TAG = "TaskActivitiesFragment_";


    private static final String TAG_DATE_PICKER_FRAGMENT = "activities_date_picker_fragment_tag";
    private static final String SNACKBAR_TAG = "task_activities_snackbar";
    private static final int REQUEST_CODE_PROCESS_ACTIVITY = 10005;

    @ViewById(android.R.id.list)
    RecyclerView mRecyclerView;

    @ViewById(R.id.progressLayout)
    ProgressLayout mProgressLayout;

    @ViewById(R.id.contentRoot)
    RelativeLayout mContentRootView;

    @ViewById(R.id.filter)
    TaskActivitiesFilterView mFilterView;

    @ViewById(R.id.container)
    FrameLayout mContainerView;

    @FragmentArg(EXTRA_TASK_ID)
    Long mExtraTaskId;

    @FragmentArg(EXTRA_TITLE)
    String mExtraTitle;

    @InstanceState
    int mListPosition = -1;

    @InstanceState
    int mListPositionOffset;

    @InstanceState
    boolean mIsFilterPanelShown;

    @Inject
    Bus mBus;

    @Inject
    @Named(ApplicationModule.HANDLER_MAIN)
    Handler mHandler;

    @StringRes(R.string.no_activity)
    String mNoActivityMessage;

    @StringRes(R.string.no_filtered_activity)
    String mNoFilteredActivityMessage;

    private ActionBar mActionBar;
    private TaskActivitiesAdapter mAdapter;
    private Menu mOptionsMenu;
    private TaskTimeSpanActions mTaskTimeSpanActions;
    private FloatingActionButton mFloatingActionButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Injection.sUiComponent.injectTaskActivitiesFragment(this);
        mTaskTimeSpanActions = new TaskTimeSpanActions(getActivity(), savedInstanceState);
        mBus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTaskTimeSpanActions.dispose();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mTaskTimeSpanActions.saveState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFilterView != null) {
            mFilterView.updateDateView();
        }
    }

    @Override
    public void onDestroyView() {
        mBus.unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mAdapter.getItemCount() > 0) {
            TaskActivitiesLayoutManager layoutManager = (TaskActivitiesLayoutManager) mRecyclerView.getLayoutManager();
            mListPosition = layoutManager.findFirstVisibleItemPosition();
            mListPositionOffset = (mListPosition != RecyclerView.NO_POSITION)
                    ? layoutManager.findItemOffset(mListPosition) : 0;
        }
    }


    @AfterViews
    void bindViews() {
        bindFloatingButton();

        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        if (mExtraTitle != null) {
           mActionBar.setTitle(mExtraTitle);
        }

        mRecyclerView.setHasFixedSize(false);
        final TaskActivitiesLayoutManager layoutManager = new TaskActivitiesLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new TaskActivitiesAdapter(getActivity());
        mAdapter.setOnSelectActiveListener(this);
        mRecyclerView.setAdapter(mAdapter);

        mFilterView.setVisibility(View.INVISIBLE);
        mFilterView.setOnFilterListener(this);
        if (mIsFilterPanelShown) {
            showFilterView(false);
        } else {
            hideFilterView(false);
        }

        mProgressLayout.setShouldDisplayEmptyIndicatorMessage(true);
        mProgressLayout.setEmptyIndicatorStyle(Typeface.ITALIC);
        final Resources res = getResources();
        mProgressLayout.setEmptyIndicatorTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen.empty_indicator_text_size));
        mProgressLayout.setEmptyIndicatorTextColor(res.getColor(R.color.empty_indicator));
        mProgressLayout.setProgressLayoutCallbacks(
                new ProgressLayout.JobProgressLayoutCallbacks(JobSelector.forJobTags(LOADER_TAG)) {
                    @Override
                    public boolean hasContent() {
                        return mAdapter.getItemCount() > 0;
                    }

                });
        requestLoad(LOADER_TAG, this);
        mProgressLayout.updateProgressView();
        DatePickerDialog dialog = (DatePickerDialog)
                getChildFragmentManager().findFragmentByTag(TAG_DATE_PICKER_FRAGMENT);
        if (dialog != null) {
            dialog.setOnDateSetListener(this);
        }

        mTaskTimeSpanActions.bind(mAdapter, mRecyclerView);
        mTaskTimeSpanActions.setOnActivatedListener(sender -> {
            hideFilterView(true);
            updateOptionsMenu();
            mFloatingActionButton.setVisibility(View.INVISIBLE);
        });
        mTaskTimeSpanActions.setOnDeactivatedListener(sender -> {
            mFloatingActionButton.setVisibility(View.VISIBLE);
        });
        mTaskTimeSpanActions.setOnEditListener(sender -> editSelectedSpan());
        mTaskTimeSpanActions.setOnDidRemoveListener(sender -> requestLoad(LOADER_TAG, this));
        mTaskTimeSpanActions.setOnDidRestoreSpansListener(sender -> requestLoad(LOADER_TAG, this));
    }

    void bindFloatingButton() {
        final ViewGroup floatingButtonContainer = (ViewGroup) LayoutInflater.from(getActivity())
                .inflate(R.layout.view_floating_action_button, mContentRootView, false);
        mFloatingActionButton = (FloatingActionButton) floatingButtonContainer.findViewById(R.id.floatingButton);
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mContentRootView.addView(floatingButtonContainer, params);
        mFloatingActionButton.attachToRecyclerView(mRecyclerView);
        mFloatingActionButton.setOnClickListener(this::onFloatingButtonClicked);
        mFloatingActionButton.setOnLongClickListener(this::onFloatingActionButtonLongClicked);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_task_activities, menu);
        mOptionsMenu = menu;
        updateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.actionToggleFilter:
                toggleFilterView();
                updateOptionsMenu();
                return true;
            case R.id.actionShowTaskActivity:
                showTaskActivity();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnJobSuccess(LoadTaskActivitiesJob.class)
    public void onLoadSuccess(LoadJobResult<List<TaskActivityItem>> result) {
        mAdapter.setItems(result.getData());
        mProgressLayout.updateProgressView();
        if (mListPosition >=0) {
            TaskActivitiesLayoutManager layoutManager = (TaskActivitiesLayoutManager) mRecyclerView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(mListPosition, mListPositionOffset);
            mListPosition = -1;
            mListPositionOffset = 0;
        }
        if (mAdapter.getItemCount() == 0) {
            mProgressLayout.setEmptyIndicatorMessage(mFilterView.getFilterState().isEmpty()
                    ? mNoActivityMessage : mNoFilteredActivityMessage);
        }
    }

    @OnJobFailure(LoadTaskActivitiesJob.class)
    public void onLoadFailed() {
        showToast(R.string.error_unable_to_load_task_activities);
    }

    @Override
    public Job onCreateJob(String s) {
        if (LOADER_TAG.equals(s)) {
            LoadTaskActivitiesJob job = Injection.sJobsComponent.loadTaskActivitiesJob();
            job.setGroupId(JobManager.JOB_GROUP_UNIQUE);
            job.setTaskId(mExtraTaskId);
            job.addTag(LOADER_TAG);
            TaskActivitiesFilterView.FilterState filterState = mFilterView.getFilterState();
            if (filterState != null) {
                job.getFilter().startDateMillis(filterState.startDateMillis)
                        .endDateMillis(filterState.endDateMillis)
                        .period(filterState.period);
            }
            return job;
        }

        throw new UnsupportedOperationException("Unknown job id");
    }

    @Subscribe
    public void onTaskActivityUpdated(TaskActivityUpdateEvent event) {
        final long taskId = event.getActiveTaskInfo().getTask().getId();
        if (mExtraTaskId != taskId) {
            return;
        }

        mAdapter.updateCurrentActivityTime(taskId);
    }

    @Override
    public void onSelectDateClicked(Calendar selectedDate) {
        Snackbar snackbar = SnackbarManager.getCurrentSnackbar();
        if (snackbar != null && snackbar.isShowing()) {
            snackbar.dismiss();
            mHandler.postDelayed(() -> {
                if (isAdded()) {
                    showDatePickerDialog(selectedDate);
                }
            }, Consts.DISMISS_DELAY_MILLIS);
        } else {
            showDatePickerDialog(selectedDate);
        }
    }

    @Override
    public void onFilterChanged(TaskActivitiesFilterView.FilterState filterState) {
        dismissSnackbar();
        JobManager.getInstance().cancelAll(JobSelector.forJobTags(LOADER_TAG));
        requestLoad(LOADER_TAG, this);
    }

    @Override
    public void onFilterReset() {
        dismissSnackbar();
        hideFilterView(true);
        updateOptionsMenu();
        requestLoad(LOADER_TAG, this);
    }

    @Override
    public void onIncorrectDateSet(int datePanelType) {
        if (datePanelType == DatePeriodView.DATE_PANEL_NONE) return;
        int textRes;
        final Activity activity = getActivity();
        if (DeviceUtils.isTabletDevice(activity) || DeviceUtils.isLandscapeOrientation(activity)) {
            textRes = (datePanelType == DatePeriodView.DATE_PANEL_START)
                    ? R.string.incorrect_filter_start_date
                    : R.string.incorrect_filter_end_date;
        } else {
            textRes = (datePanelType == DatePeriodView.DATE_PANEL_START)
                    ? R.string.incorrect_filter_start_date_short
                    : R.string.incorrect_filter_end_date_short;
        }
        Snackbar bar = Snackbar.with(getActivity())
                .text(textRes)
                .actionLabel(R.string.button_choose)
                .colorResource(R.color.lightRed)
                .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                .type(SnackbarType.MULTI_LINE)
                .actionListener((snackbar) -> onChooseDateButtonClicked())
                .animation(true);

        bar.setTag(SNACKBAR_TAG);
        SnackbarManager.show(bar);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        if (!isFilterPanelVisible()) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        long date = calendar.getTimeInMillis();
        mFilterView.setDate(date);
    }

    private void updateOptionsMenu() {
        if (mOptionsMenu == null) {
            return;
        }

        MenuItem item = mOptionsMenu.findItem(R.id.actionToggleFilter);
        if (item == null) {
            return;
        }

        if (isFilterPanelVisible()) {
            item.setIcon(R.drawable.ic_filter_remove_white_24dp);
            item.setTitle(R.string.action_toggle_filter_off);
        } else if (mFilterView.getFilterState().isEmpty()) {
            item.setIcon(R.drawable.ic_filter_white_24dp);
            item.setTitle(R.string.action_toggle_filter_on);
        } else {
            item.setIcon(R.drawable.ic_filter_red_24dp);
            item.setTitle(R.string.action_toggle_filter_on);
        }
    }

    private void showFilterView(boolean animate) {
        mIsFilterPanelShown = true;

        if (isFilterPanelVisible()) {
            return;
        }

        if (animate) {
            TransitionSet set = new TransitionSet();
            set.addTransition(new Fade(Fade.IN));
            set.addTransition(new ChangeBounds());
            set.setInterpolator(new DecelerateInterpolator(0.8f));
            set.setOrdering(TransitionSet.ORDERING_TOGETHER);
            set.excludeTarget(R.id.floatingButton, true);
            TransitionManager.beginDelayedTransition(mContentRootView, set);
        }
        updateFilterViewSize();
        mFilterView.setVisibility(View.VISIBLE);
    }

    private void hideFilterView(boolean animate) {
        mIsFilterPanelShown = false;

        if (!isFilterPanelVisible()) {
            return;
        }

        if (animate) {
            TransitionSet set = new TransitionSet();
            set.addTransition(new Fade(Fade.OUT));
            set.addTransition(new ChangeBounds());
            set.setInterpolator(new DecelerateInterpolator(0.8f));
            set.setOrdering(TransitionSet.ORDERING_TOGETHER);
            set.excludeTarget(R.id.floatingButton, true);
            TransitionManager.beginDelayedTransition(mContentRootView, set);
        }
        updateFilterViewSize();
        mFilterView.setVisibility(View.INVISIBLE);
    }

    private void updateFilterViewSize() {
        RelativeLayout.LayoutParams containerLayoutParams =
                (RelativeLayout.LayoutParams) mContainerView.getLayoutParams();

        int measuredHeight;
        if (mIsFilterPanelShown) {
            measuredHeight = mFilterView.getMeasuredHeight();
            if (measuredHeight < 1) {
                int maxHeight = getResources().getDisplayMetrics().heightPixels;
                int spec = View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST);
                mFilterView.measure(spec, spec);
                measuredHeight = mFilterView.getMeasuredHeight();
            }
        } else {
            measuredHeight = 0;
        }
        containerLayoutParams.topMargin = measuredHeight;
        mContainerView.setLayoutParams(containerLayoutParams);
    }


    private boolean isFilterPanelVisible() {
        return mFilterView.getVisibility() == View.VISIBLE;
    }

    private void toggleFilterView() {
        if (isFilterPanelVisible()) {
            dismissSnackbar();
            hideFilterView(true);
        } else {
            showFilterView(true);
        }
    }

    private void showTaskActivity() {
        Bundle args = new Bundle();
        if (mFilterView != null && mFilterView.getFilterState() != null) {
            List<Long> taskId = new ArrayList<>();
            taskId.add(mExtraTaskId);
            args.putParcelable(StatsDetailsFragment.EXTRA_TASK_FILTER, TaskLoadFilter.fromTaskActivitiesFilter(mFilterView
                    .getFilterState())
                    .taskIds(taskId));
        }

        args.putInt(StatsDetailsFragment.EXTRA_CHART_VIEW_TYPE, StatisticsViewBinder.VIEW_TYPE_ACTIVITY_TIMELINE);
        args.putString(StatsDetailsFragment.EXTRA_TASK_TITLE, mExtraTitle);

        Intent launchIntent = FragmentContainerActivity.prepareLaunchIntent(
                getActivity(), StatsDetailsFragment_.class.getName(), args);
        getActivity().startActivityForResult(launchIntent, 1000);
    }

    private void onChooseDateButtonClicked() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mFilterView.getSelectedDateInitialValue());
        onSelectDateClicked(calendar);
    }

    private void showDatePickerDialog(Calendar selectedDate) {
        DatePickerDialog dialog = DatePickerDialog.newInstance(
                this,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH),
                false);
        dialog.show(getChildFragmentManager(), TAG_DATE_PICKER_FRAGMENT);
    }

    private void dismissSnackbar() {
        Snackbar snackbar = SnackbarManager.getCurrentSnackbar();
        if (snackbar != null && snackbar.isShowing()) {
            snackbar.dismiss();
        }
    }

    private void onFloatingButtonClicked(View v) {
        processTimeSpan(null);
    }

    private void processTimeSpan(TaskTimeSpan spanOrNull) {
        long id = (spanOrNull != null) ? spanOrNull.getId() :  EditTaskActivityDialogFragment.CREATE_NEW_SPAN_ID;

        Bundle args = new Bundle();
        args.putString(EditTaskActivityDialogFragment.EXTRA_TITLE, mExtraTitle);
        args.putLong(EditTaskActivityDialogFragment.EXTRA_SPAN_ID, id);
        args.putLong(EditTaskActivityDialogFragment.EXTRA_TASK_ID, mExtraTaskId);

        Intent launchIntent = DialogContainerActivity.prepareDialogLaunchIntent(
                getActivity(), EditTaskActivityDialogFragment.class.getName(), args);
        startActivityForResult(launchIntent, REQUEST_CODE_PROCESS_ACTIVITY);
    }

    private void editSelectedSpan() {
        List<TaskTimeSpan> selected = mAdapter.getSelectedSpans();
        Preconditions.checkArgument(selected.size() == 1, "there should be 1 selected span");
        processTimeSpan(selected.get(0));
    }

    @OnActivityResult(REQUEST_CODE_PROCESS_ACTIVITY)
    public void onEditActivityResult(int resultCode, Intent data) {
        if (resultCode == EditTaskActivityDialogFragment.RESULT_CODE_OK) {
            requestLoad(LOADER_TAG, this);
        }
    }

    private boolean onFloatingActionButtonLongClicked(View v) {
        showToast(R.string.hint_new_time_span);
        return true;
    }

    @Override
    public void onSelectActive(long id) {
        final Intent intent = new Intent();
        intent.putExtra(TaskTimeSpan.class.getName(), id);
        getActivity().setResult(ViewTaskFragment.REQUEST_CODE_VIEW_ACTIVITIES, intent);
        getActivity().finish();
    }
}
