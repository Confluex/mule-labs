> This documentation is heavily under construction so check back for updates.

# Confluex Performance Tools

Initially, we're providing instrumentation to show visibility into mule message processor (and hopefully other
internals) performance.

# Viewing the Report

> We'll be offering JSON and HTML version of this report. For now, it's just an HTML table dump of statistics
> provided by JAMon's MonitorFactory.getReport. Look for this soon

You can download an Excel report of the performance stats via URL :

http://my.mule.server:9138/performance

![Performance Report](PerformanceExcelReport.png)



# Project Configuration

Create your aspectJ configuration file. This is where you tell it what packages to instrument

 **MULE_HOME/conf/META-INF/aop.xml**

```xml
<!DOCTYPE aspectj PUBLIC
        "-//AspectJ//DTD//EN" "http://www.eclipse.org/aspectj/dtd/aspectj.dtd">
<aspectj>
    <weaver options="-showWeaveInfo -debug -verbose -XmessageHandlerClass:org.springframework.aop.aspectj.AspectJWeaverMessageHandler">
        <include within="org.mule..*"/>
        <include within="com.mycompany..*"/>
    </weaver>
    <aspects>
        <aspect name="com.confluex.mule.performance.PerformanceAspect"/>
        <include within="com.mycompany..*"/>
    </aspects>
</aspectj>
```

You can add the dependencies to you pom:

```xml
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjrt</artifactId>
    <version>${aspectj.version}</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>${aspectj.version}</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>com.confluex.mule</groupId>
    <artifactId>confluex-performance-tools</artifactId>
    <version>${confluex.labs.version}</version>
</dependency>
```

If you want access to the HTTP services/reports, you'll need to import the performance.xml flows from one of your
flows:

```xml
    <spring:beans>
        <spring:import resource="classpath:performance.xml"/>
    </spring:beans>
```

# Automating Performance Testing

Add the aspect weaver into the build **if you need to instrument from functional test cases**


```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.16</version>
    <configuration>
        <argLine>
            -javaagent:${env.HOME}/.m2/repository/org/aspectj/aspectjweaver/${aspectj.version}/aspectjweaver-${aspectj.version}.jar
        </argLine>
        <systemProperties>
            <property>
                <name>aj.weaving.verbose</name>
                <value>true</value>
            </property>
        </systemProperties>
    </configuration>
</plugin>
```


For now, use the JAMon MonitorFactory in your test cases.

```groovy
import static com.jamonapi.LogMonitor.*

def outboxEndpoints = getMonitor("TestPerformanceTools.AbstractEndpoint.outbox", "ms.")
assert outboxEndpoints.hits = 7
assert outboxEndpoints.avg < 25
```

> Currently, the convention is "Flow.ClassName.name" which is not consistently derived right now. This will be refined
> soon to offer better testing. This area needs work badly.




# Mule Server Configuration

Bootstrap the mule server with the javaavgent provided by the container:

> Double check the version that is provided with your Mule ditribution

**MULE_HOME/conf/wrapper.conf**

```ini
wrapper.java.additional.<n>=-javaagent:%MULE_HOME%/lib/opt/aspectjweaver-1.6.11.jar
```

# IDE Integration

See your IDE documentation for setting test parameters and set the path to the aspectjweaver jar as a -javagent.

