package com.simbirsoft.timemeter.events;

import com.simbirsoft.timemeter.db.model.Tag;

import java.util.List;

public class ImportTagsEvent {

    public List<Tag> tags;

    public ImportTagsEvent(List<Tag> tags) {
        this.tags = tags;
    }
}
