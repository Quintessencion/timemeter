package com.simbirsoft.timemeter.ui.taskedit;

import android.content.Context;
import android.transitions.everywhere.Scene;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.simbirsoft.timemeter.R;
import org.apmem.tools.layouts.FlowLayout;

/**
 * Created by Ismailov Alexander on 14.04.15.
 */
public class TaskViewScene {

    final ViewGroup layout;
    final Scene scene;
    final FlowLayout tagViewContainer;

    static TaskViewScene create(Context context, ViewGroup parentView) {
        return new TaskViewScene(context, parentView);
    }

    protected TaskViewScene(Context context, ViewGroup parentView) {
        layout = (ViewGroup) LayoutInflater.from(context).inflate(
                R.layout.fragment_view_task_scene_root, parentView, false);
        scene = new Scene(parentView, layout);

        tagViewContainer = (FlowLayout) layout.findViewById(R.id.tagViewContainer);
    }
}
