package com.confluex.mule.performance;

import com.jamonapi.MonitorFactory;
import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

public class PerformanceReport implements Callable {
    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        return MonitorFactory.getReport();
    }

}
