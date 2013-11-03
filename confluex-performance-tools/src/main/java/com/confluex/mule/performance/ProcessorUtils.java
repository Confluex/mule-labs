package com.confluex.mule.performance;

import org.mule.api.NamedObject;
import org.mule.api.transformer.Transformer;
import org.mule.endpoint.AbstractEndpoint;


public class ProcessorUtils {

    public static String resolveProcessorName(Object processor) {
        if (processor instanceof AbstractEndpoint) {
            return getProcessorName((AbstractEndpoint) processor);
        }
        if (processor instanceof Transformer) {
            return getProcessorName((Transformer) processor);
        }
        return formatName("MessageProcessor", processor.getClass().getSimpleName());
    }

    public static String getProcessorName(NamedObject namedObject) {
        return formatName("NamedObject", namedObject.getName());
    }

    public static String getProcessorName(AbstractEndpoint endpoint) {
        return formatName("AbstractEndpoint", endpoint.getName());
    }

    public static String getProcessorName(Transformer transformer) {
        return formatName("Transformer", transformer.getName());
    }


    public static String formatName(String type, String name) {
        return "[" + type + "] " + name;
    }

}
