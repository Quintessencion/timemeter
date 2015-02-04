package com.simbirsoft.timemeter.ui.taskedit;


import android.content.Context;
import android.transitions.everywhere.Scene;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.ui.views.TagAutoCompleteTextView;

public class TaskTagsEditScene {

    final View layout;
    final Scene scene;
    final EditText descriptionEditView;
    final TagAutoCompleteTextView tagAutoCompleteView;

    static TaskTagsEditScene create(Context context, ViewGroup parentView) {
        return new TaskTagsEditScene(context, parentView);
    }

    protected TaskTagsEditScene(Context context, ViewGroup parentView) {
        layout = LayoutInflater.from(context).inflate(
                R.layout.fragment_edit_task_scene_root, parentView, false);
        scene = new Scene(parentView, layout);
        descriptionEditView = (EditText) layout.findViewById(android.R.id.edit);
        tagAutoCompleteView = (TagAutoCompleteTextView) layout.findViewWithTag(R.id.tagSearchView);
    }
}
