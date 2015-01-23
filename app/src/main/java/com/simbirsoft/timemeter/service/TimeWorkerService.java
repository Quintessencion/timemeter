package com.simbirsoft.timemeter.service;

import android.content.Intent;

import com.be.android.library.worker.controllers.ThreadPoolWorker;
import com.be.android.library.worker.interfaces.Worker;
import com.be.android.library.worker.service.WorkerService;
import com.simbirsoft.timemeter.Consts;

public class TimeWorkerService extends WorkerService {

    @Override
    protected Worker createWorker(Intent intent) {
        final ThreadPoolWorker worker = new ThreadPoolWorker(Consts.WORKER_THREAD_POOL_COUNT);

        return worker;
    }
}
