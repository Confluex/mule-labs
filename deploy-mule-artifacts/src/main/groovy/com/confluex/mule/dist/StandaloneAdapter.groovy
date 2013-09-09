package com.confluex.mule.dist

import com.confluex.mule.dist.util.Artifact
import groovy.util.logging.Slf4j

import javax.xml.xpath.XPathFactory
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource


@Slf4j
class StandaloneAdapter {
    final ZipInputStream standaloneZip
    List<Artifact> artifacts
    def builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    def xpath = XPathFactory.newInstance().newXPath()

    StandaloneAdapter(InputStream standaloneInputStream) {
        standaloneZip = new ZipInputStream(standaloneInputStream)
    }

    List<Artifact> getArtifacts() {
        if (artifacts) return artifacts

        def jars = []
        ZipEntry entry = standaloneZip.nextEntry
        while (entry) {
            if (entry.name =~ /\.jar/) {
                def artifact = consumeJar(entry, standaloneZip)
                osgiVersionHack(artifact)
                if (! (artifact.path =~ /groovy-all/)) jars << artifact
            }
            entry = standaloneZip.nextEntry
        }
        return (artifacts = jars)
    }

    private void osgiVersionHack(Artifact artifact) {
        if (artifact.path =~ /osgi\.jar/) {
            if (artifact.pom && !artifact.pom.version.endsWith('osgi')) artifact.pom.version += '-osgi'
        }
    }

    Artifact consumeJar(ZipEntry jarInfo, InputStream zip) {
        def artifact = new Artifact()
        artifact.path = jarInfo.name
        JarInputStream jar = new JarInputStream(zip)
        JarEntry entry = jar.nextJarEntry
        while (entry) {
            if (entry.name =~ /META-INF\/.*\/pom\.xml/) {
                artifact.pom = parsePom(jar)
            }
            entry = jar.nextJarEntry
        }
        artifact
    }

    def parsePom(InputStream pom) {
        byte[] rawPom = readButDontClose(pom)
        def xml = builder.parse(new InputSource(new StringReader(new String(rawPom)))).documentElement
        [
            groupId: xpath.evaluate('/project/groupId', xml) ?: xpath.evaluate('/project/parent/groupId', xml),
            artifactId: xpath.evaluate('/project/artifactId', xml),
            version: xpath.evaluate('/project/version', xml)
        ]
    }

    byte[] readButDontClose(InputStream stream) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream()
        bytes << stream
        bytes.toByteArray()
    }
}
