package com.simbirsoft.timemeter.jobs;

import com.be.android.library.worker.base.BaseJob;
import com.be.android.library.worker.base.JobEvent;
import com.simbirsoft.timemeter.controller.ITaskActivityManager;
import com.simbirsoft.timemeter.controller.ActiveTaskInfo;
import com.simbirsoft.timemeter.log.LogFactory;

import org.slf4j.Logger;

import javax.inject.Inject;

public class UpdateTaskActivityTimerJob extends BaseJob {

    public static final class UpdateTimerEvent extends JobEvent {
        private ActiveTaskInfo mActiveTaskInfo;

        UpdateTimerEvent(ActiveTaskInfo activeTaskInfo) {
            setEventCode(EVENT_CODE_UPDATE);

            mActiveTaskInfo = activeTaskInfo;
        }

        public ActiveTaskInfo getActiveTaskInfo() {
            return mActiveTaskInfo;
        }
    }

    private static final Logger LOG = LogFactory.getLogger(UpdateTaskActivityTimerJob.class);

    private static final int UPDATE_INTERVAL_MILLIS = 1000;

    private final ITaskActivityManager mTaskActivityManager;

    public UpdateTaskActivityTimerJob(ITaskActivityManager taskActivityManager) {
        mTaskActivityManager = taskActivityManager;

        // Execute job in dedicated group
        setGroupId(JobConsts.JOB_GROUP_UPDATE_ACTIVITY_TIMER);
    }

    @Override
    protected JobEvent executeImpl() throws Exception {
        while (true) {
            if (isCancelled()) {
                LOG.debug("task activity update timer cancelled");
                break;
            }

            ActiveTaskInfo info = mTaskActivityManager.getActiveTaskInfo();
            if (!mTaskActivityManager.hasActiveTask()) {
                LOG.debug("no active task found");
                break;
            }

            notifyJobEvent(new UpdateTimerEvent(info));

            Thread.sleep(UPDATE_INTERVAL_MILLIS);
        }

        LOG.debug("task activity updates finished");

        return JobEvent.ok();
    }
}
