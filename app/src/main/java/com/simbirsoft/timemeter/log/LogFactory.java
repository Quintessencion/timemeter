package com.simbirsoft.timemeter.log;

import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

public final class LogFactory {

    public LogFactory(Class<?>[] ignoredClasses) {
        sIgnoredClasses = ignoredClasses;
    }

    private static Class<?>[] sIgnoredClasses = new Class<?>[0];

    private static String makeName(Class<?> clazz) {
        return clazz.getSimpleName();
    }

    public static Logger getLogger(Class<?> clazz) {
        for (Class<?> logClass : sIgnoredClasses) {
            if (clazz.isAssignableFrom(logClass)) {
                return NOPLogger.NOP_LOGGER;
            }
        }

        return org.slf4j.LoggerFactory.getLogger(makeName(clazz));
    }
}
