package com.simbirsoft.timemeter.persist;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "tag-ref")
public class XmlTagRef {

    @Attribute(name = "ref", required = true)
    private long tagId;

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }
}
