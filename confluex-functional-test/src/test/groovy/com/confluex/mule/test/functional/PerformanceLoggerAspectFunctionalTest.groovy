package com.confluex.mule.test.functional

import org.junit.Test
import org.mule.tck.junit4.FunctionalTestCase


class PerformanceLoggerAspectFunctionalTest extends FunctionalTestCase {
    @Override
    protected String getConfigResources() {
        return "test-performance-tools.xml"
    }

    @Test
    void shouldCaptureProcessorMetrics() {
        def client = muleContext.client
        def payload = [
                "hello",
                "hello",
                "hello",
                "hello",
                "hello",
                "hello",
                "hello",
                "hello",
                "hello",
                "hello"
        ]
        client.dispatch("inbox", payload, [:])

        10.times {
            def msg = client.request("outbox", 5000)
            assert msg?.payload == "hello"
        }
    }
}
