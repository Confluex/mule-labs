package com.confluex.mule.test.functional.components

import groovy.util.logging.Slf4j
import org.mule.api.MuleEventContext
import org.mule.api.MuleMessage
import org.mule.api.lifecycle.Callable

@Slf4j
class SleepyComponent implements Callable {
    @Override
    MuleMessage onCall(MuleEventContext eventContext) throws Exception {
        log.debug("Yawwwnn.. I'm taking a quick nap.")
        sleep(100)
        return eventContext.getMessage()
    }
}
