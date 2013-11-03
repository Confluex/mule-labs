package com.confluex.mule.performance;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.mule.api.MuleEvent;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.transformer.Transformer;
import org.mule.endpoint.AbstractEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class PerformanceLoggerAspect {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Around(value = "call(public * org.mule.api.processor.MessageProcessor+.process(..)) && target(processor) && args(event)", argNames = "pjp,event,processor")
    public MuleEvent captureMessageProcessorStats(ProceedingJoinPoint pjp, MuleEvent event, MessageProcessor processor) throws Throwable {
        return captureStats(ProcessorUtils.resolveProcessorName(processor), pjp);
    }

    @Around(value = "call(public * org.mule.endpoint.AbstractEndpoint+.process(..)) && target(endpoint) && args(event)", argNames = "pjp,event,endpoint")
    public MuleEvent captureEndpointStats(ProceedingJoinPoint pjp, MuleEvent event, AbstractEndpoint endpoint) throws Throwable {
        return captureStats(ProcessorUtils.resolveProcessorName(endpoint), pjp);
    }

    @Around(value = "call(public * org.mule.api.transformer.Transformer+.transform(..)) && target(endpoint) && args(msg)", argNames = "pjp,msg,endpoint")
    public MuleEvent captureEndpointStats(ProceedingJoinPoint pjp, Object msg, Transformer endpoint) throws Throwable {
        return captureStats(ProcessorUtils.resolveProcessorName(endpoint), pjp);
    }

    protected MuleEvent captureStats(String name,  ProceedingJoinPoint pjp) throws Throwable {
        log.info("Capturing statistics for message processor: {}", name);
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
