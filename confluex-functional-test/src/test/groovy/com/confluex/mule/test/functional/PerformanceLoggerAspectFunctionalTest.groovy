package com.confluex.mule.test.functional

import com.sun.jersey.api.client.Client
import groovy.util.logging.Slf4j
import org.junit.Test
import org.mule.tck.junit4.FunctionalTestCase

import static com.jamonapi.MonitorFactory.*

@Slf4j
class PerformanceLoggerAspectFunctionalTest extends FunctionalTestCase {
    protected static final String DEFAULT_JMON_UNIT = "ms."

    @Override
    protected String getConfigResources() {
        return "test-performance-tools.xml"
    }

    @Test
    void shouldCaptureProcessorMetrics() {
        def client = muleContext.client
        def payload = [
                "Dan",
                "Joe",
                "Bill",
                "Jane"
        ]
        client.dispatch("inbox", payload, [:])

        4.times {
            def msg = client.request("outbox", 5000)
            assert payload.find { "Hello ${it}" == msg.payloadAsString }
        }
        def setPayloadTransformers = getMonitor("TestPerformanceTools.MessageProcessor.SetPayloadTransformer", DEFAULT_JMON_UNIT)
        def inboxEndpoints = getMonitor("MuleClient.AbstractEndpoint.inbox", DEFAULT_JMON_UNIT)
        def outboxEndpoints = getMonitor("TestPerformanceTools.AbstractEndpoint.outbox", DEFAULT_JMON_UNIT)
        def foreachProcessors = getMonitor("TestPerformanceTools.MessageProcessor.Foreach", DEFAULT_JMON_UNIT)

        // TODO Needs isolation. When ran as a suite, these values will be larger due to the monitoring of all flows.
        assert inboxEndpoints.hits >= 1
        assert foreachProcessors.hits >= 1
        assert setPayloadTransformers.hits >= 4
        assert outboxEndpoints.hits >= 4

        def report = Client.create().resource("http://localhost:9138/performance").get(String.class)
        log.info(report)
    }
}
