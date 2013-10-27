package com.confluex.mule.performance;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.mule.api.MuleEvent;
import org.mule.api.NamedObject;
import org.mule.api.processor.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class PerformanceLoggerAspect {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Around(value = "call(public * org.mule.api.processor.MessageProcessor+.process(..)) && target(processor) && args(event)", argNames = "pjp,event,processor")
    public MuleEvent captureMessageProcessorStats(ProceedingJoinPoint pjp, MuleEvent event, MessageProcessor processor) throws Throwable {
        String name = processor instanceof NamedObject ? ((NamedObject) processor).getName() : processor.getClass().getName();
        log.debug("Capturing statistics for message processor: {}", name);
        Monitor monitor = MonitorFactory.start(name);
        MuleEvent result = null;
        try {
            result = (MuleEvent) pjp.proceed();
        } finally {
            monitor.stop();
        }
        return result;
    }

}
