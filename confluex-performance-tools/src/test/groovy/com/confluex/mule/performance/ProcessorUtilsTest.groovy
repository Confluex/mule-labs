package com.confluex.mule.performance

import org.junit.Test
import org.mule.api.NamedObject
import org.mule.api.transformer.Transformer
import org.mule.endpoint.AbstractEndpoint

import static org.mockito.Mockito.*

class ProcessorUtilsTest {


    @Test
    void shouldFormatUnknownMessageProcessorTypes() {
        assert ProcessorUtils.resolveProcessorName(new Object()) == "[MessageProcessor] Object"
    }

    @Test
    void shouldFormatAbstractEndpointProcessors() {
        def endpoint = mock(AbstractEndpoint)
        when(endpoint.getName()).thenReturn("foo")
        assert ProcessorUtils.resolveProcessorName(endpoint) == "[AbstractEndpoint] foo"
    }

    @Test
    void shouldFormatNamedObjectProcessors() {
        def endpoint = mock(NamedObject)
        when(endpoint.getName()).thenReturn("foo")
        assert ProcessorUtils.resolveProcessorName(endpoint) == "[NamedObject] foo"
    }

    @Test
    void shouldFormatTransformers() {
        def transformer = mock(Transformer)
        when(transformer.getName()).thenReturn("foo")
        assert ProcessorUtils.resolveProcessorName(transformer) == "[Transformer] foo"
    }

    @Test
    void shouldFormatProcessorsThatHaveNullNames() {
        def endpoint = mock(AbstractEndpoint)
        assert ProcessorUtils.resolveProcessorName(endpoint) == "[AbstractEndpoint] null"
    }
}
