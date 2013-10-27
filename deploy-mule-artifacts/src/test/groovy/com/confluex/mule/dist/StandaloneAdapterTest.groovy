package com.confluex.mule.dist

import groovy.util.logging.Slf4j
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

@Slf4j
class StandaloneAdapterTest {

    StandaloneAdapter adapter

    @Before
    void init() {
        adapter = new StandaloneAdapter(Thread.currentThread().contextClassLoader.getResourceAsStream('tmp/mule-ee-distribution-standalone-3.4.1.zip'))
    }

    @Test
    void shouldFindJarsInStandaloneDistribution() {
        assert 382 == adapter.artifacts.size()
    }

    @Test
    void shouldFindPomsInJars() {
        log.debug "A jar without a pom is ${adapter.artifacts.find { it.pom == null }.path}"
        assert 211 == adapter.artifacts.count { it.pom != null }
    }

    @Test
    void shouldParseGroupIdInPoms() {
        assert 26 == adapter.artifacts.count { it.pom?.groupId == 'org.mule.modules' }
    }

    @Test
    void shouldFindAFewKeyArtifacts() {
        assert adapter.artifacts.find { it.pom?.groupId == 'org.mule' && it.pom?.artifactId == 'mule-core' }
        assert adapter.artifacts.find { it.pom?.groupId == 'org.mule.modules' && it.pom?.artifactId == 'mule-module-pgp' }
        assert adapter.artifacts.find { it.pom?.groupId == 'org.mule.transports' && it.pom?.artifactId == 'mule-transport-wmq-ee' }
        assert adapter.artifacts.find { it.pom?.groupId == 'commons-jxpath' && it.pom?.artifactId == 'commons-jxpath' && it.pom?.version == '1.3-osgi' }
    }

    @Test
    void shouldIgnoreGroovyAllJar() {
        assert adapter.artifacts.every { ! (it.path =~ /groovy-all.*\.jar/ ) }
    }

    @Test
    void shouldIncludeJar() {
        def expectedSize = [
                'ognl-2.7.3-osgi.jar': 242337,
                'mule-transport-jms-3.4.1.jar': 158335,
                'mule-module-drools-3.4.1.jar': 7613,
                'log4j-1.2.16.jar': 481535,
                'opensaml-2.5.1-1.jar': 1352924,
                'mule-tests-functional-3.4.1.jar': 88180
        ]
        adapter.artifacts.each {
            if (expectedSize.containsKey(it.name)) {
                assert expectedSize[it.name] == it.jar.length
            }
        }
    }
}
