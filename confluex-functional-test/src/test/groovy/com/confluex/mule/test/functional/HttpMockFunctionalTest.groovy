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

    /**
     * The Mule Configuration file(s) to load when starting the embedded Mule server
     */
    protected String getConfigResources() {
        return "test-mock-http-config.xml"
    }

    /**
     * Create a new Jetty server before each test and assign our MockHttpRequest handler to process
     * requests.
     */
    @Before
    void createMockHttpServer() {
        server = new Server(9001)
        handler = new MockHttpRequestHandler()
        server.handler = handler
        server.start()
        server.stopAtShutdown = true
    }

    /**
     * Stop the server between each test to ensure all of the connections are cleaned up.
     */
    @After
    void stopMockHttpServer() {
        sleep(100)
        server.stop()
    }

    /**
     * Retrieve the catalog XML and assert interactions
     */
    @Test
    void shouldListCatalog() {
        handler.when("/catalog")
                .thenReturnResource("/payloads/catalog.xml")
                .withStatus(200)

        def message = muleContext.client.send("requestCatalog", "", [:])
        assert message.payloadAsString == this.class.getResourceAsStream("/payloads/catalog.xml").text

        handler.verify("/catalog", MethodExpectation.GET)
    }

    /**
     * Update the catalog with XML and assert interactions
     */
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

        // vm endpoint is async, we need to wait until the handler processes
        // the request or times out (error condition)
        assert handler.waitForEvents(1, 1000)

        // now we can do verifications
        handler.verify("/catalog",
                MethodExpectation.PUT,
                MediaTypeExpectation.XML,
                new HeaderExpectation("updatedBy", "Bill Murray")
        )
    }
}
