package com.simbirsoft.timemeter.ui.settings;

import android.os.Bundle;
import android.os.Environment;

import com.be.android.library.worker.annotations.OnJobFailure;
import com.be.android.library.worker.annotations.OnJobSuccess;
import com.be.android.library.worker.controllers.JobLoader;
import com.be.android.library.worker.interfaces.Job;
import com.be.android.library.worker.models.LoadJobResult;
import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.injection.Injection;
import com.simbirsoft.timemeter.jobs.LoadTagListJob;
import com.simbirsoft.timemeter.jobs.LoadTaskBundleJob;
import com.simbirsoft.timemeter.jobs.LoadTaskListJob;
import com.simbirsoft.timemeter.persist.XmlCreatorUtil;
import com.simbirsoft.timemeter.persist.XmlTaskList;
import com.simbirsoft.timemeter.ui.base.BaseFragment;
import com.simbirsoft.timemeter.ui.model.TaskBundle;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_load_tasks)
public class LoadTasksFragment extends BaseFragment implements JobLoader.JobLoaderCallbacks {

    private static final int LOAD_TASK_JOB_ID = 2970017;

    private static final String TASK_LIST_LOADER_TAG = "SettingsFragment_taskLoaderTag";
    private static final String TAGS_LIST_LOADER_TAG = "SettingsFragment_tagsLoaderTag";
    private static final String TASK_BUNDLE_LIST_LOADER_TAG = "SettingsFragment_taskBundleLoaderTag";

    private List<TaskBundle> mTaskBundles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTaskBundles = new ArrayList<>();
        requestLoad(TASK_LIST_LOADER_TAG, this);
    }

    @AfterViews
    public void bindViews() {
        requestLoad(TASK_LIST_LOADER_TAG, this);
    }

    @Override
    public Job onCreateJob(String loaderAttachTag) {
        switch (loaderAttachTag) {
            case TAGS_LIST_LOADER_TAG:
                return Injection.sJobsComponent.loadTagListJob();
            case TASK_LIST_LOADER_TAG:
                LoadTaskListJob job = Injection.sJobsComponent.loadTaskListJob();
                job.setGroupId(LOAD_TASK_JOB_ID);
                job.addTag(loaderAttachTag);
                return job;
            case TASK_BUNDLE_LIST_LOADER_TAG:

            default:
                throw new IllegalArgumentException();
        }
    }

    @SuppressWarnings("unused")
    @OnJobSuccess(LoadTaskListJob.class)
    public void onTaskListLoaded(LoadJobResult<List<TaskBundle>> event) {
        mTaskBundles = event.getData();
        requestLoad(TAGS_LIST_LOADER_TAG, this);
    }

    @SuppressWarnings("unused")
    @OnJobFailure(LoadTaskListJob.class)
    public void onTaskListLoadFailed() {
        // TODO: impl error handling
    }

    @SuppressWarnings("unused")
    @OnJobSuccess(LoadTagListJob.class)
    public void onTagListLoaded(LoadJobResult<List<Tag>> result) {
        XmlTaskList xmlTaskList = XmlCreatorUtil.taskBundleToXmlTaskList(mTaskBundles);
        xmlTaskList.setTagList(XmlCreatorUtil.tagsToXmlTags(result.getData()));

        Serializer serializer = new Persister();

        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "person.xml";

        File file = new File(sdPath);

        try {
            serializer.write(xmlTaskList, file);
        } catch (Exception e) {
            // TODO: impl error handling
        }
    }

    @SuppressWarnings("unused")
    @OnJobFailure(LoadTagListJob.class)
    public void onTagListLoadFailed() {
        // TODO: impl error handling
    }


    @SuppressWarnings("unused")
    @OnJobSuccess(LoadTaskBundleJob.class)
    public void onTaskBundlesLoaded(LoadJobResult<List<TaskBundle>> event) {
        mTaskBundles = event.getData();
        requestLoad(TASK_BUNDLE_LIST_LOADER_TAG, this);
    }

    @SuppressWarnings("unused")
    @OnJobFailure(LoadTaskBundleJob.class)
    public void onTaskBundleLoadFailed() {
        // TODO: impl error handling
    }
}
