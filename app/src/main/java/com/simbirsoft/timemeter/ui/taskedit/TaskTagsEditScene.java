package com.simbirsoft.timemeter.ui.taskedit;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.transitions.everywhere.ChangeBounds;
import android.transitions.everywhere.CircularPropagation;
import android.transitions.everywhere.Fade;
import android.transitions.everywhere.Scene;
import android.transitions.everywhere.Transition;
import android.transitions.everywhere.TransitionManager;
import android.transitions.everywhere.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.simbirsoft.timemeter.R;
import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.ui.views.TagAutoCompleteTextView;
import com.squareup.phrase.Phrase;

public class TaskTagsEditScene {

    final ViewGroup layout;
    final Scene scene;
    final TagAutoCompleteTextView tagsView;
    final RecyclerView tagsRecyclerView;
    final TextView createTagView;
    boolean isFinishing;

    static TaskTagsEditScene create(Context context, ViewGroup parentView) {
        return new TaskTagsEditScene(context, parentView);
    }

    protected TaskTagsEditScene(Context context, ViewGroup parentView) {
        layout = (ViewGroup) LayoutInflater.from(context).inflate(
                R.layout.fragment_edit_task_scene_tags, parentView, false);
        scene = new Scene(parentView, (View)layout);
        tagsView = (TagAutoCompleteTextView) layout.findViewById(R.id.tagSearchView);
        createTagView = (TextView) layout.findViewById(R.id.createTagView);
        createTagView.setVisibility(View.GONE);
        tagsRecyclerView = (RecyclerView) layout.findViewById(android.R.id.list);
    }

    void showCreateTagView(String tagName) {
        final String text = Phrase.from("{title} \"{tag_name}\"")
                .put("title", layout.getContext().getString(R.string.action_create_tag))
                .put("tag_name", tagName)
                .format()
                .toString();

        createTagView.setText(text);

        if (createTagView.getVisibility() != View.VISIBLE) {
            TransitionSet set = new TransitionSet()
                .setInterpolator(new AccelerateInterpolator())
                .setOrdering(TransitionSet.ORDERING_TOGETHER)
                .addTransition(new Fade())
                .addTransition(new ChangeBounds())
                .addTarget(createTagView)
                .addTarget(tagsRecyclerView);
            set.setPropagation(new CircularPropagation());
            TransitionManager.beginDelayedTransition(layout, set);
            createTagView.setVisibility(View.VISIBLE);
        }
    }

    void hideCreateTagView() {
        TransitionSet set = new TransitionSet()
                .setInterpolator(new AccelerateInterpolator())
                .setOrdering(TransitionSet.ORDERING_TOGETHER)
                .addTransition(new Fade())
                .addTransition(new ChangeBounds())
                .addTarget(createTagView)
                .addTarget(tagsRecyclerView);
        set.setPropagation(new CircularPropagation());
        TransitionManager.beginDelayedTransition(layout, set);
        createTagView.setVisibility(View.GONE);
    }
}
