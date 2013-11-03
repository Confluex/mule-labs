package com.confluex.mule.performance

import org.junit.Test
import org.mule.endpoint.AbstractEndpoint

import static org.mockito.Mockito.*

class ProcessorUtilsTest {

    @Test
    void shouldFormatUnknownMessageProcessorTypes() {
        assert ProcessorUtils.resolveProcessorName(new Object()) == "MessageProcessor.Object"
    }

    @Test
    void shouldFormatAbstractEndpointProcessors() {
        def endpoint = mock(AbstractEndpoint)
        when(endpoint.getName()).thenReturn("foo")
        assert ProcessorUtils.resolveProcessorName(endpoint) == "AbstractEndpoint.foo"
    }

}
