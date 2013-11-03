[![Build Status](https://travis-ci.org/Confluex/confluex-mule-extensions.png?branch=master)](https://travis-ci.org/Confluex/confluex-mule-extensions)

# Confluex Mule Extensions

This is a sandbox which we use to test and enhance ideas for libraries for Mule extensions. If they mature, they will
be promoted to a top level project or as a pull request into the Mule project itself.

Until then, feel free to use them. We'll try to keep breaking changes to a minimum.

**Table of Contents**

* [Module: confluex-test-notifications] (#confluex-test-notifications)
* [Module: confluex-performance-tools] (#confluex-performance-tools)
* [Using the Extensions] (#using-the-extensions)
* [License] (#License)

**Groovy Examples**

Most of the examples documented here are using Groovy instead of Java. Feel free to use Java if you wish. There is
no Groovy requirement (your Mule container comes with Groovy and you should really check it out though!).

# confluex-test-notifications

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

# confluex-performance-tools

Initially, we're providing instrumentation to show visibility into mule message processor (and hopefully other
internals) performance.

**Example**

TODO (see tests for now)


# Using the Extensions

The artifacts are available via Maven. Simply add them to your pom. As we add connectors, transports, etc., they will
be available via the Mule Studio pallet.

Maven Artifact:

```xml
<dependency>
    <groupId>com.confluex.mule</groupId>
    <artifactId>confluex-test-notifications</artifactId>
    <version>0.1.0</version>
</dependency>
```

For Snapshots, you can use the Sonatype OSS repository:

```xml
    <repository>
        <id>sonatype-snapshots</id>
        <name>Sonatype OSS Maven Repo (snapshots)</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
        <releases>
            <enabled>false</enabled>
        </releases>
    </repository>
```

# License

   Copyright 2013 Confluex, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
