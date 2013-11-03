package com.confluex.mule.performance;

import org.mule.endpoint.AbstractEndpoint;


public class ProcessorUtils {

    public static String resolveProcessorName(Object processor) {
        if (processor instanceof AbstractEndpoint) {
            return getProcessorName((AbstractEndpoint) processor);
        }
        return formatName("MessageProcessor", processor.getClass().getSimpleName());
    }

    public static String getProcessorName(AbstractEndpoint endpoint) {
        return formatName("AbstractEndpoint", endpoint.getName());
    }

    public static String formatName(String type, String name) {
        return type + "." + name;
    }

}
