package com.confluex.mule.test.functional

import org.junit.Test
import org.mule.tck.junit4.FunctionalTestCase

import static com.jamonapi.MonitorFactory.*

class PerformanceLoggerAspectFunctionalTest extends FunctionalTestCase {
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
        def namedObjects = getMonitor("[NamedObject] null", "ms.")
        def transformers = getMonitor("[Transformer] SetPayloadTransformer", "ms.")
        def inboxEndpoints = getMonitor("[AbstractEndpoint] inbox", "ms.")
        def outboxEndpoints = getMonitor("[AbstractEndpoint] outbox", "ms.")
        def outboundTimeoutProcessors = getMonitor("[MessageProcessor] OutboundEventTimeoutMessageProcessor", "ms.")

        // TODO Needs refactor. when ran as a suite, these values will be larger due to the monitoring of all flows.
        assert namedObjects.hits >= 32
        assert inboxEndpoints.hits >= 1
        assert transformers.hits >= 4
        assert outboxEndpoints.hits >= 4
        assert outboundTimeoutProcessors.hits >= 5
//        assert namedObjects.hits == 32
//        assert inboxEndpoints.hits == 1
//        assert transformers.hits == 4
//        assert outboxEndpoints.hits == 4
//        assert outboundTimeoutProcessors.hits == 5
    }
}
