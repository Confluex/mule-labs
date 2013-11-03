[![Build Status](https://travis-ci.org/Confluex/mule-labs.png?branch=master)](https://travis-ci.org/Confluex/mule-labs)

# Confluex Mule Labs

This is a sandbox which we use to test and enhance ideas for libraries for Mule. If they mature, they will
be promoted to a top level project or as a pull request into the Mule project itself.

Until then, feel free to use them. We'll try to keep breaking changes to a minimum.

**Table of Contents**

* [Modules] (#modules)
* [Using the Extensions] (#using-the-extensions)
* [License] (#License)

**Groovy Examples**

Most of the examples documented here are using Groovy instead of Java. Feel free to use Java if you wish. There is
no Groovy requirement (your Mule container comes with Groovy and you should really check it out though!).

# Modules

*confluex-test-notifications(

Mule Testing utilities, etc.

[Learn More](confluex-test-notifications)

*confluex-performance-tools*

Initially, we're providing instrumentation to show visibility into mule message processor (and hopefully other
internals) performance.

[Learn More](confluex-performance-tools)


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
