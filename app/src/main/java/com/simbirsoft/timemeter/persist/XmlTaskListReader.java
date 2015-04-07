package com.simbirsoft.timemeter.persist;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;

public class XmlTaskListReader {

    public static XmlTaskList readXml(InputStream input) throws Exception {
        Serializer serializer = new Persister();

        return serializer.read(XmlTaskList.class, input);
    }
}