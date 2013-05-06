package com.confluex.mule.test.functional

import com.confluex.mule.test.http.MockHttpRequestHandler
import com.confluex.mule.test.http.expectations.HeaderExpectation
import com.confluex.mule.test.http.expectations.MediaTypeExpectation
import com.confluex.mule.test.http.expectations.MethodExpectation
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mortbay.jetty.Server
import org.mule.tck.junit4.FunctionalTestCase

class HttpMockFunctionalTest extends FunctionalTestCase {

    Server server
    MockHttpRequestHandler handler

    @Override
    protected String getConfigResources() {
        return "test-mock-http-config.xml"
    }

    @Before
    void createMockHttpServer() {
        server = new Server(9001)
        handler = new MockHttpRequestHandler()
        server.handler = handler
        server.start()
        server.stopAtShutdown = true
    }

    @After
    void stopMockHttpServer() {
        sleep(100) // gives mule a bit of time to finish shutting its connections down.. not required but reduces log noise
        server.stop()
    }

    @Test
    void shouldListCatalog() {
        handler.when("/catalog")
                .thenReturnResource("/payloads/catalog.xml")
                .withStatus(200)

        def message = muleContext.client.send("requestCatalog", "", [:])
        assert message.payloadAsString == this.class.getResourceAsStream("/payloads/catalog.xml").text

        handler.verify("/catalog", MethodExpectation.GET)
    }

    @Test
    void shouldUpdateCatalog() {
        handler.when("/catalog")
                .thenReturnText("Updated")
                .withStatus(302)
                .withHeader("Location", "http://localhost:9001/catalog")


        def payload = [
                [id: 1, name: "Super Widget"],
                [id: 2, name: "Super Gadget"]
        ]
        muleContext.client.dispatch("updateCatalog", payload, [:])

        // vm endpoint is async, need to wait until the endpoint has been called this time
        assert handler.waitForEvents(1, 100000)
        // now we can do verifications after the http calls
        println handler.getRequests("/catalog").headers
        handler.verify("/catalog",
                MethodExpectation.PUT,
                MediaTypeExpectation.XML,
                new HeaderExpectation("updatedBy", "Bill Murray")
        )
    }
}
