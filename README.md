# Confluex Mule Extensions

This is a library of extension to Mule code. It contains utility code for testing, etc.

This library is still under heavy development. Feel free to use, contribute but there could be changes to
the API until it reaches 1.0 status. Of course, we'll try to keep breaking changes to a minimum.

# Modules

The Confluex Mule Extension is composed of several modules:

## confluex-test-http

Mock HTTP testing library for mocking out interaction to HTTP endpoints from within Mule FunctionalTestCase (although
it could be used without mule at all).

<script src="http://gist-it.appspot.com/github/Confluex/confluex-mule-extensions/master/confluex-functional-test/src/test/groovy/com/confluex/mule/test/functional/BlockingEndpointListenerFunctionalTest.groovy"></script>


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