package com.yangdb.fuse.rendering;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.ExternalMetadata;
import org.jooby.MediaType;
import org.jooby.Renderer;
import org.jooby.Status;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoggingJacksonRendererTest {

    @Test
    public void testBoolMessage() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MetricRegistry metricRegistry = Mockito.mock(MetricRegistry.class);
        Timer timer = Mockito.mock(Timer.class);
        Timer.Context time = Mockito.mock(Timer.Context.class);
        when(metricRegistry.timer(any())).thenReturn(timer);
        when(timer.time()).thenReturn(time);
        when(time.stop()).thenReturn((long) 10000000);
        LoggingJacksonRenderer renderer = new LoggingJacksonRenderer(mapper, MediaType.json, Mockito.mock(Logger.class), metricRegistry);
        ContentResponse<Object> abc123456789 = ContentResponse.Builder.builder(Status.ACCEPTED, Status.NOT_FOUND).data(Optional.of(true)).requestId("ABC123456789").external(new ExternalMetadata()).elapsed(9).compose();
        Renderer.Context context = Mockito.mock(Renderer.Context.class);
        when(context.length(anyLong())).thenReturn(context);
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        renderer.renderValue(abc123456789, context);
        verify(context).send(captor.capture());

        String str = new String(captor.getValue());
        ContentResponse<Boolean> contentResponse = mapper.readValue(str, ContentResponse.class);
        Assert.assertEquals(9, Integer.parseInt(contentResponse.getElapsed()));
        Assert.assertTrue(contentResponse.getData());
        Assert.assertEquals(10, Integer.parseInt(contentResponse.getRenderElapsed()));
        Assert.assertEquals(19, Integer.parseInt(contentResponse.getTotalElapsed()));
    }

    @Test
    public void testStringMessage() throws Exception {
        MetricRegistry metricRegistry = Mockito.mock(MetricRegistry.class);
        Timer timer = Mockito.mock(Timer.class);
        Timer.Context time = Mockito.mock(Timer.Context.class);
        when(metricRegistry.timer(any())).thenReturn(timer);
        when(timer.time()).thenReturn(time);
        when(time.stop()).thenReturn((long) 10000000);
        ObjectMapper mapper = new ObjectMapper();
        LoggingJacksonRenderer renderer = new LoggingJacksonRenderer(mapper, MediaType.json, Mockito.mock(Logger.class), metricRegistry);
        ContentResponse<Object> abc123456789 = ContentResponse.Builder.builder(Status.ACCEPTED, Status.NOT_FOUND).data(Optional.of("bla bla")).requestId("ABC123456789").external(new ExternalMetadata()).elapsed(9).compose();
        Renderer.Context context = Mockito.mock(Renderer.Context.class);
        when(context.length(anyLong())).thenReturn(context);
        renderer.renderValue(abc123456789, context);
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(context).send(captor.capture());

        String str = new String(captor.getValue());
        ContentResponse<String> contentResponse = mapper.readValue(str, ContentResponse.class);
        Assert.assertEquals(9, Integer.parseInt(contentResponse.getElapsed()));
        Assert.assertEquals("bla bla",contentResponse.getData());
        Assert.assertEquals(10, Integer.parseInt(contentResponse.getRenderElapsed()));
        Assert.assertEquals(19, Integer.parseInt(contentResponse.getTotalElapsed()));

    }
}
