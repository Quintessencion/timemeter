package com.simbirsoft.timemeter.persist;

import com.google.common.collect.Lists;

import java.util.List;

public class XmlTagListConverter {

    public static List<Long> asList(List<XmlTagRef> xmlTagRefList) {
        List<Long> coll = Lists.newArrayListWithCapacity(xmlTagRefList.size());
        for (XmlTagRef xmlTagRef: xmlTagRefList) {
            coll.add(xmlTagRef.getTagId());
        }
        return coll;
    }
}