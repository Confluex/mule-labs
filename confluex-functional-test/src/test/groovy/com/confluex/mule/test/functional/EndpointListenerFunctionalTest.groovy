package com.confluex.mule.test.functional

import com.confluex.mule.test.event.EndpointListener
import org.junit.Test
import org.mule.tck.junit4.FunctionalTestCase

class EndpointListenerFunctionalTest extends FunctionalTestCase {

    @Override
    protected String getConfigResources() {
        return "test-endpoint-listener-config.xml"
    }

    @Test
    void shouldWaitForSingleMessage() {
        def listener = new EndpointListener("out")
        muleContext.registerListener(listener)
        muleContext.client.dispatch("in", "Bacon", [:])
        assert listener.waitForMessage()
        assert listener.messages.first().payloadAsString == 'Bacon'
    }

    @Test
    void shouldTimeOutIfNumberOfMessagesIsLessThanExpected() {
        def listener = new EndpointListener("out", 2)
        muleContext.registerListener(listener)
        muleContext.client.dispatch("in", "Bacon", [:])
        assert !listener.waitForMessage(2000)
        assert listener.messages.first().payloadAsString == 'Bacon'
    }
}
