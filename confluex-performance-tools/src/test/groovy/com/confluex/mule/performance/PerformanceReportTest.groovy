package com.confluex.mule.performance

import com.jamonapi.MonitorFactory
import org.junit.Test
import org.mule.api.MuleEventContext

import static org.mockito.Mockito.*

class PerformanceReportTest {

    @Test
    void shouldPrintStatusForAllComponents() {
        def monitor = MonitorFactory.start("test")
        try {
            sleep(100)
        } finally {
            monitor.stop()
        }
        def report = new PerformanceReport().onCall(mock(MuleEventContext)) as String
        def xml = new XmlSlurper().parseText(report)
        assert xml.th.size() == 17
        assert xml.tr.size() == 1
    }
}
