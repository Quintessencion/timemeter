package com.simbirsoft.timeactivity.events;


import com.simbirsoft.timeactivity.db.model.Tag;

import java.util.List;

public class ImportTagsEvent {

    public List<Tag> tags;

    public ImportTagsEvent(List<Tag> tags) {
        this.tags = tags;
    }
}
