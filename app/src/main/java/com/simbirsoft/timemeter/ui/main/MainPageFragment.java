package com.simbirsoft.timemeter.ui.main;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.EventListener;
import com.simbirsoft.timemeter.Consts;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.SaveTaskBundleJob;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.model.TaskBundle;
import com.simbirsoft.timemeter.ui.model.TaskChangedEvent;
import com.simbirsoft.timemeter.ui.taskedit.EditTaskFragment;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

public class MainPageFragment extends BaseFragment {
    public class SnackbarShowEvent {
        private boolean mVisible;

        public SnackbarShowEvent(boolean visible) {
            mVisible = visible;
        }

        public boolean isVisible() {
            return mVisible;
        }
    }

    private static final String SNACKBAR_TAG = "main_page_snackbar";

    private boolean mIsContentInvalidated;
    private boolean mIsSelected;

    @Inject
    Bus mBus;

    protected Bus getBus() {
        return mBus;
    }

    public void onSelect() {
        mIsSelected = true;
        if (mIsContentInvalidated) {
            mIsContentInvalidated = false;
            reloadContent();
        }
    }

    public void onDeselect() {
        mIsSelected = false;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    protected void sendTaskChangedEvent(int resultCode) {
        mBus.post(new TaskChangedEvent(resultCode));
    }

    @Subscribe
    public void onTaskChanged(TaskChangedEvent event) {
        if (mIsSelected && event.getResultCode() == EditTaskFragment.RESULT_CODE_TASK_RECREATED) {
            reloadContent();
            return;
        }
        if (!mIsSelected && (event.getResultCode() == EditTaskFragment.RESULT_CODE_TASK_RECREATED
                                ||needUpdateAfterTaskChanged(event.getResultCode()))) {
            mIsContentInvalidated = true;
        }
    }

    protected boolean needUpdateAfterTaskChanged(int resultCode) {
        return true;
    }

    @OnJobSuccess(SaveTaskBundleJob.class)
    public void onTaskSaved() {
        if (mIsSelected) {
            reloadContent();
        }
        mBus.post(new TaskChangedEvent(EditTaskFragment.RESULT_CODE_TASK_RECREATED));
    }

    @OnJobFailure(SaveTaskBundleJob.class)
    public void onTaskSaveFailed() {
        Snackbar bar = Snackbar.with(getActivity())
                .text(R.string.error_unable_to_backup_task)
                .colorResource(R.color.lightRed)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE);
        if (getRecyclerView() != null) {
            bar.attachToRecyclerView(getRecyclerView());
        }
        bar.setTag(SNACKBAR_TAG);
        SnackbarManager.show(bar);
    }

    protected void showTaskRemoveUndoBar(TaskBundle bundle) {
        mBus.post(new SnackbarShowEvent(true));
        final Snackbar bar = Snackbar.with(getActivity())
                .type(SnackbarType.MULTI_LINE)
                .actionLabel(R.string.action_undo_remove)
                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                .color(getResources().getColor(R.color.primaryDark))
                .actionListener((snackbar) -> backupRemovedTask(bundle, snackbar))
                .animation(true)
                .eventListener(new EventListener() {
                    @Override
                    public void onShow(Snackbar snackbar) {
                    }

                    @Override
                    public void onShown(Snackbar snackbar) {
                    }

                    @Override
                    public void onDismiss(Snackbar snackbar) {
                    }

                    @Override
                    public void onDismissed(Snackbar snackbar) {
                        mBus.post(new SnackbarShowEvent(false));
                    }
                });
        if (getRecyclerView() != null) {
            bar.attachToRecyclerView(getRecyclerView());
        }

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                final String description;

                if (bundle.hasPersistedState()) {
                    description = bundle.createOriginalBundle().getTask().getDescription();
                } else {
                    description = bundle.getTask().getDescription();
                }

                final String undoMessage = getString(R.string.hint_task_removed)
                        + "\n"
                        + description;

                final SpannableStringBuilder sb = new SpannableStringBuilder(undoMessage);
                final StyleSpan iss = new StyleSpan(android.graphics.Typeface.ITALIC);
                sb.setSpan(iss, undoMessage.length() - description.length(),
                        undoMessage.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                return sb.toString();
            }

            @Override
            protected void onPostExecute(String text) {
                if (isAdded() && !getActivity().isFinishing()) {
                    bar.text(text);
                    SnackbarManager.show(bar);
                }
            }
        }.execute();
    }

    private void backupRemovedTask(TaskBundle taskBundle, Snackbar snackbar) {
        long delay = 0;
        if (snackbar != null) {
            delay = Consts.DISMISS_DELAY_MILLIS;
            snackbar.dismiss();
        }

        final AsyncTask unmarshallTask = new AsyncTask<Void, Void, TaskBundle>() {
            @Override
            protected TaskBundle doInBackground(Void... voids) {
                return taskBundle.createOriginalBundle();
            }
        }.execute();

        getView().postDelayed(() -> {
            try {
                SaveTaskBundleJob job = Injection.sJobsComponent.saveTaskBundleJob();
                job.setTaskBundle((TaskBundle) unmarshallTask.get());
                submitJob(job);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, delay);
    }


    protected RecyclerView getRecyclerView() {
        return null;
    }

    protected void reloadContent() {

    }
}
