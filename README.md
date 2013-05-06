# Confluex Mule Extensions

This is a library of extension to Mule code. It contains utility code for testing, etc.

This library is still under heavy development. Feel free to use, contribute but there could be changes to
the API until it reaches 1.0 status. Of course, we'll try to keep breaking changes to a minimum.

# Modules

The Confluex Mule Extension is composed of several modules:

## confluex-test-http

Mock HTTP testing library for mocking out interaction to HTTP endpoints from within Mule FunctionalTestCase (although
it could be used without mule at all).

## confluex-test-notifications

Since the MuleFunctionalTest runs in a separate thread from the server, it's often
difficult to determine when the messages are finished and when to start your assertions (usually by waiting or
sending the messages to a mock endpoint).

This module contains event listeners for endpoints which you can use to block until the required messages have
passed before making your assertions.

# Using the Extensions

Maven Artifact:

```xml
<dependency>
    <groupId>com.confluex.mule</groupId>
    <artifactId>confluex-test-http</artifactId>
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