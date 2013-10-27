package com.confluex.mule.test.functional

import com.confluex.mule.test.event.BlockingEndpointListener
import org.junit.Test
import org.mule.tck.junit4.FunctionalTestCase

class BlockingEndpointListenerFunctionalTest extends FunctionalTestCase {

    @Override
    protected String getConfigResources() {
        return "test-endpoint-listener-config.xml"
    }

    @Test
    void shouldWaitForSingleMessage() {
        def listener = new BlockingEndpointListener("out")
        muleContext.registerListener(listener)
        muleContext.client.dispatch("in", "Bacon", [:])
        assert listener.waitForMessages()
        assert listener.messages.first().payloadAsString == 'Bacon'
    }

    @Test
    void shouldTimeOutIfNumberOfMessagesIsLessThanExpected() {
        def listener = new BlockingEndpointListener("out", 2)
        muleContext.registerListener(listener)
        muleContext.client.dispatch("in", "Bacon", [:])
        assert !listener.waitForMessages(2000)
        assert listener.messages.first().payloadAsString == 'Bacon'
    }
}
