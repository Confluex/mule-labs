package com.confluex.mule.test.functional

import com.confluex.mock.http.MockHttpServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mule.tck.junit4.FunctionalTestCase

import static com.confluex.mock.http.matchers.HttpMatchers.*

class HttpMockFunctionalTest extends FunctionalTestCase {

    MockHttpServer server

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
        server = new MockHttpServer(9001)
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
        server.respondTo(get("/catalog"))
                .withResource("/payloads/catalog.xml")
                .withStatus(200)

        def message = muleContext.client.send("requestCatalog", "", [:])
        assert message.payloadAsString == this.class.getResourceAsStream("/payloads/catalog.xml").text

        def requests = server.requests?.findAll { it.method == "GET" && it.path == "/payloads/catalog.xml" }
        assert requests.size() == 1
    }

    /**
     * Update the catalog with XML and assert interactions
     */
    @Test
    void shouldUpdateCatalog() {
        server.respondTo(put("/catalog"))
                .withBody("Updated")
                .withStatus(302)
                .withHeader("Location", "http://localhost:9001/catalog")


        def payload = [
                [id: 1, name: "Super Widget"],
                [id: 2, name: "Super Gadget"]
        ]
        muleContext.client.dispatch("updateCatalog", payload, [:])

        // vm endpoint is async, we need to wait until the handler processes
        // the request or times out (error condition)
        assert server.waitFor(put("/catalog"), 1, 10000)

        // you can also get access to the raw client request data if desired
        def requests = server.requests.findAll { it.path == "/catalog" && it.method == "PUT" }
        assert requests.size() == 1
        assert requests[0].headers['Content-Type'] == "application/xml"
        assert requests[0].headers['X-MULE_ENDPOINT'] == "http://localhost:9001/catalog"
        assert requests[0].headers.updatedBy == "Bill Murray"
        assert requests[0].method == "PUT"
        assert requests[0].body.startsWith("<list>")

    }
}
