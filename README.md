[![Build Status](https://travis-ci.org/Confluex/confluex-mule-extensions.png?branch=master)](https://travis-ci.org/Confluex/confluex-mule-extensions)

# Confluex Mule Extensions

This is a library of extension to Mule code. It contains utility code for testing, etc.

This library is still under heavy development. Feel free to use, contribute but there could be changes to
the API until it reaches 1.0 status. Of course, we'll try to keep breaking changes to a minimum.

**Table of Contents**

* [Module: confluex-test-http] (#confluex-test-http)
* [Module: confluex-test-notifications] (#confluex-test-notifications)
* [Using the Extensions] (#using-the-extensions)

## confluex-test-http

Mock HTTP testing library for mocking out interaction to HTTP endpoints from within Mule FunctionalTestCase (although
it could be used without mule at all).

_Mule Configuration_

```xml
<?xml version="1.0" encoding="UTF-8"?>
<mule xmlns="http://www.mulesoft.org/schema/mule/core"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:vm="http://www.mulesoft.org/schema/mule/vm"
      xmlns:http="http://www.mulesoft.org/schema/mule/http"
      xmlns:mulexml="http://www.mulesoft.org/schema/mule/xml"
      xmlns:context="http://www.springframework.org/schema/context"
      xsi:schemaLocation="
        http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
        http://www.mulesoft.org/schema/mule/vm http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd
        http://www.mulesoft.org/schema/mule/xml http://www.mulesoft.org/schema/mule/xml/current/mule-xml.xsd
        http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd
        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
        ">

    <context:property-placeholder location="classpath:test-mock-http-config.properties"/>

    <vm:endpoint name="requestCatalog" path="catalog.request" exchange-pattern="request-response"/>
    <vm:endpoint name="updateCatalog" path="catalog.update" exchange-pattern="one-way"/>


    <flow name="RequestCatalogFlow">
        <inbound-endpoint ref="requestCatalog"/>
        <logger level="INFO" category="RequestCatalogFlow" message="Request payload:  #[payload]"/>
        <http:outbound-endpoint host="${service.host}" port="${service.port}" path="/catalog" method="GET"/>
        <logger level="INFO" category="RequestCatalogFlow" message="Response payload:  #[payload]"/>
    </flow>
    <flow name="UpdateCatalogFlow">
        <inbound-endpoint ref="updateCatalog"/>
        <logger level="INFO" category="UpdateCatalogFlow" message="Request payload:  #[payload]"/>
        <set-property propertyName="updatedBy" value="Bill Murray"/>
        <mulexml:object-to-xml-transformer/>
        <http:outbound-endpoint host="${service.host}" port="${service.port}" path="/catalog" method="PUT"
                                contentType="application/xml"/>
        <logger level="INFO" category="UpdateCatalogFlow" message="Response payload:  #[payload]"/>
    </flow>
</mule>
```

_Functional Test Case_

```groovy

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
        assert handler.waitForEvents(1, 10000)

        // now we can do verifications
        handler.verify("/catalog",
                MethodExpectation.PUT,
                MediaTypeExpectation.XML,
                new HeaderExpectation("updatedBy", "Bill Murray")
        )


        // you can also get access to the raw client request data if desired
        def requests = handler.getRequests("/catalog")
        assert requests.size() == 1
        assert requests[0].headers['Content-Type'] == "application/xml"
        assert requests[0].headers['X-MULE_ENDPOINT'] == "http://localhost:9001/catalog"
        assert requests[0].headers.updatedBy == "Bill Murray"
        assert requests[0].method == "PUT"
        assert requests[0].body.startsWith("<list>")
    }
}

```


## confluex-test-notifications

Since the MuleFunctionalTest runs in a separate thread from the server, it's often
difficult to determine when the messages are finished and when to start your assertions (usually by waiting or
sending the messages to a mock endpoint).

**BlockingEndpointListener**

This event listener will attach to an endpoint and allow your test to wait and collect the messages sent to the endpoint.
The following example configuration and test case demonstrate the basic usage:

_Mule Configuration_


```xml
<?xml version="1.0" encoding="UTF-8"?>
<mule xmlns="http://www.mulesoft.org/schema/mule/core"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:vm="http://www.mulesoft.org/schema/mule/vm"
      xsi:schemaLocation="
        http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
        http://www.mulesoft.org/schema/mule/vm http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd
        ">

    <notifications dynamic="true">
        <notification event="ENDPOINT-MESSAGE" />
    </notifications>


    <vm:endpoint name="in" path="in" />
    <vm:endpoint name="out" path="out" />

    <flow name="Test Endpoint Listener">
        <inbound-endpoint ref="in"/>
        <logger level="INFO" message="Message content:  #[payload]"/>
        <outbound-endpoint ref="out"/>
    </flow>
</mule>
```

_Functional Test Case_

```groovy
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
        assert listener.waitForMessage()
        assert listener.messages.first().payloadAsString == 'Bacon'
    }

    @Test
    void shouldTimeOutIfNumberOfMessagesIsLessThanExpected() {
        def listener = new BlockingEndpointListener("out", 2)
        muleContext.registerListener(listener)
        muleContext.client.dispatch("in", "Bacon", [:])
        assert !listener.waitForMessage(2000)
        assert listener.messages.first().payloadAsString == 'Bacon'
    }
}
```

# Using the Extensions

Maven Artifact:

```xml
<dependency>
    <groupId>com.confluex.mule</groupId>
    <artifactId>confluex-test-http</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.confluex.mule</groupId>
    <artifactId>confluex-test-notifications</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

We're actively working on getting this project into the maven central repository. Until then, you can build it from
source or artifacts may be retrieved from the Confluex maven repository.

```xml
<repositories>
	<repository>
		<id>confluex-public-releases</id>
		<name>Confluex Releases</name>
		<url>http://dev.confluex.com/nexus/content/repositories/public-releases/</url>
		<layout>default</layout>
		<releases>
			<enabled>true</enabled>
		</releases>
		<snapshots>
			<enabled>false</enabled>
		</snapshots>
	</repository>
	<repository>
		<id>confluex-public-snapshots</id>
		<name>Confluex Snapshots</name>
		<url>http://dev.confluex.com/nexus/content/repositories/public-snapshots/</url>
		<layout>default</layout>
		<releases>
			<enabled>false</enabled>
		</releases>
		<snapshots>
			<enabled>true</enabled>
		</snapshots>
	</repository>
</repositories>
```