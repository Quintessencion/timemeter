package com.simbirsoft.timeactivity.service;

import android.content.Intent;

import com.be.android.library.worker.controllers.ThreadPoolWorker;
import com.be.android.library.worker.interfaces.Worker;
import com.be.android.library.worker.service.WorkerService;
import com.simbirsoft.timeactivity.Consts;
import com.simbirsoft.timeactivity.jobs.JobConsts;

public class TimeWorkerService extends WorkerService {

    @Override
    protected Worker createWorker(Intent intent) {
        final ThreadPoolWorker worker = new ThreadPoolWorker(Consts.WORKER_THREAD_POOL_COUNT);

        worker.allocateExecutor(JobConsts.JOB_GROUP_UPDATE_ACTIVITY_TIMER);

        return worker;
    }
}
