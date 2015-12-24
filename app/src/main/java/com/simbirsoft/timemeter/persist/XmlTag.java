package com.simbirsoft.timemeter.persist;

import com.simbirsoft.timemeter.db.model.Tag;
import com.simbirsoft.timemeter.ui.util.ColorParseUtils;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "tag")
public class XmlTag {

    @Attribute(required = true)
    private long id;

    @Element(required = true)
    private String name;

    @Attribute(required = false)
    private String color;

    public XmlTag() {}

    public XmlTag(Tag tag) {
        id = tag.getId();
        name = tag.getName();
        color = ColorParseUtils.parseColor(tag.getColor());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Tag getTag() {
        Tag tag = new Tag();
        if (id > 0) {
            tag.setId(id);
        }
        tag.setName(name);

        if (color != null) {
            tag.setColor(ColorParseUtils.parseColor(color));
        }

        return tag;
    }
}
