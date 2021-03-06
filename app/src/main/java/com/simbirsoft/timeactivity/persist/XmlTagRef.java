package com.simbirsoft.timeactivity.persist;


import com.simbirsoft.timeactivity.db.model.Tag;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "tag-ref")
public class XmlTagRef {

    @Attribute(name = "ref", required = true)
    private long tagId;

    public XmlTagRef() {}

    public XmlTagRef(Tag tag) {
        tagId = tag.getId();
    }

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }
}
