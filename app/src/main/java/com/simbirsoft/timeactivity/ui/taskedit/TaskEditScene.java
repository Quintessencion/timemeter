package com.simbirsoft.timeactivity.ui.taskedit;


import android.content.Context;
import android.transitions.everywhere.Scene;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;

import com.simbirsoft.timeactivity.R;
import com.simbirsoft.timeactivity.ui.views.TagAutoCompleteTextView;

public class TaskEditScene {

    final ViewGroup layout;
    final Scene scene;
    final EditText descriptionView;
    final TagAutoCompleteTextView tagsView;

    static TaskEditScene create(Context context, ViewGroup parentView) {
        return new TaskEditScene(context, parentView);
    }

    protected TaskEditScene(Context context, ViewGroup parentView) {
        layout = (ViewGroup) LayoutInflater.from(context).inflate(
                R.layout.fragment_edit_task_scene_root, parentView, false);
        scene = new Scene(parentView, layout);
        descriptionView = (EditText) layout.findViewById(android.R.id.edit);
        tagsView = (TagAutoCompleteTextView) layout.findViewById(R.id.tagSearchView);
    }
}
