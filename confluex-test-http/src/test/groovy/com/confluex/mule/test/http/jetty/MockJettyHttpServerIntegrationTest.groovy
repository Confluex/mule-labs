package com.confluex.mule.test.http.jetty

import com.confluex.mule.test.http.MockHttpRequestHandler
import com.confluex.mule.test.http.expectations.HeaderExpectation
import com.confluex.mule.test.http.expectations.MediaTypeExpectation
import com.confluex.mule.test.http.expectations.MethodExpectation
import com.sun.jersey.api.client.Client
import groovy.util.logging.Slf4j
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mortbay.jetty.Server

import javax.ws.rs.core.MediaType

import static javax.servlet.http.HttpServletResponse.*

@Slf4j
class MockJettyHttpServerIntegrationTest {

    Server server

    @Before
    void createServer() {
        server = new Server(9001)
        server.start()
    }

    @After
    void stopServer() {
        server.stop()
    }

    @Test
    void shouldServeSimpleContent() {

        def handler = new MockHttpRequestHandler()
                .when("/foo")
                .thenReturnResource("/http/responses/foo.xml")
                .when("/bar")
                .thenReturnResource("/http/responses/bar.json")
                .withStatus(SC_OK)
                .withHeader("x-bender", "Who are you, and why should I care?")
                .withHeader("x-fry", "I did do the nasty in the pasty")


        server.handler = handler

        2.times {
            def xml = Client.create().resource("http://localhost:9001/foo")
                    .accept(MediaType.APPLICATION_XML)
                    .type(MediaType.APPLICATION_XML)
                    .get(String.class)
            assert xml == this.class.getResourceAsStream("/http/responses/foo.xml").text
        }
        3.times {
            def json = Client.create().resource("http://localhost:9001/bar")
                    .type(MediaType.APPLICATION_JSON)
                    .post(String.class, '{"count":1}')
            assert json == this.class.getResourceAsStream("/http/responses/bar.json").text
        }
        10.times {
            def txt = Client.create().resource("http://localhost:9001/random")
                    .type(MediaType.TEXT_PLAIN)
                    .put(String.class, "hi")
            assert txt == "ok"
        }

        handler.verify("/foo", MethodExpectation.GET, MediaTypeExpectation.XML)
        handler.verify("/bar",
                MethodExpectation.POST,
                MediaTypeExpectation.JSON,
                new HeaderExpectation("Host", "localhost:9001")
        )
        handler.verify("/random", MethodExpectation.PUT, MediaTypeExpectation.TEXT)

        def requests = handler.getRequests("/foo")
        assert requests.size() == 2
        requests.each {
            assert it.contentType == "application/xml"
            assert it.headers["Host"] == "localhost:9001"
            assert it.headers["Accept"] == "application/xml"
        }


        assert handler.getRequests("/bar").size() == 3
        assert handler.getRequests("/random").size() == 10

    }


}
