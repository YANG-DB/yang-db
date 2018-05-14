package com.kayhut.fuse.rendering;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Bytes;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.ElapsedFrom;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import com.kayhut.fuse.dispatcher.logging.LogType;
import com.kayhut.fuse.dispatcher.logging.MethodName;
import com.kayhut.fuse.model.transport.ContentResponse;
import org.jooby.MediaType;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogType.start;
import static com.kayhut.fuse.dispatcher.logging.LogType.success;

public class JacksonLoggingRenderer extends JacksonBaseRenderer {
    public static final String mapperParameter = "JacksonLoggingRenderer.@mapper";
    public static final String typeParameter = "JacksonLoggingRenderer.@type";
    public static final String loggerParameter = "JacksonLoggingRenderer.@logger";

    //region Constructors
    @Inject
    public JacksonLoggingRenderer(
            @Named(mapperParameter) ObjectMapper mapper,
            @Named(typeParameter) MediaType type,
            @Named(loggerParameter) Logger logger,
            MetricRegistry metricRegistry) {
        super(mapper, type);

        this.logger = logger;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region JacksonBaseRenderer Implementation
    @Override
    public void render(final Object value, final Context ctx) throws Exception {
        if (ctx.accepts(this.type) && value instanceof ContentResponse) {
            ctx.type(this.type);
            this.renderValue(value, ctx);
        }
    }

    protected void renderValue(final Object value, final Context ctx) throws Exception {
        new LogMessage.Impl(this.logger, trace, "start renderValue", LogType.of(start), renderValue, ElapsedFrom.now()).log();

        Timer.Context timerContext = this.metricRegistry.timer(MetricRegistry.name(this.getClass().getName(), renderValue.toString())).time();
        byte[] bytes = this.mapper.writeValueAsBytes(value);

        long renderElapsed = 0;

        byte[] tmpBytes = null;

        int indexOfRenderElapsed = Bytes.indexOf(bytes, renderElapsedBytes);
        if (indexOfRenderElapsed >= 0) {
            tmpBytes = new byte[2 * (renderElapsedBytes.length + columnBytes.length + sampleValueBytes.length)];

            renderElapsed = TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS);
            byte[] renderElapsedValueBytes = String.format("%08d", renderElapsed).getBytes();

            System.arraycopy(bytes, indexOfRenderElapsed, tmpBytes, 0, Math.min(tmpBytes.length, bytes.length - indexOfRenderElapsed + 1));
            indexOfRenderElapsed += Bytes.indexOf(tmpBytes, zeroBytes);

            System.arraycopy(renderElapsedValueBytes, 0, bytes, indexOfRenderElapsed, renderElapsedValueBytes.length);
        }

        int indexOfTotalElapsed = Bytes.indexOf(bytes, totalElapsedBytes);
        if (indexOfTotalElapsed >= 0) {
            if (tmpBytes == null) {
                tmpBytes = new byte[2 * (renderElapsedBytes.length + columnBytes.length + sampleValueBytes.length)];
            }

            long totalElapsed = value != null && value instanceof ContentResponse ?
                    Long.parseLong(((ContentResponse)value).getElapsed()) + renderElapsed :
                    0;

            byte[] totalElapsedValueBytes = String.format("%08d", totalElapsed).getBytes();

            System.arraycopy(bytes, indexOfTotalElapsed, tmpBytes, 0, Math.min(tmpBytes.length, bytes.length - indexOfTotalElapsed + 1));
            indexOfTotalElapsed += Bytes.indexOf(tmpBytes, zeroBytes);

            System.arraycopy(totalElapsedValueBytes, 0, bytes, indexOfTotalElapsed, totalElapsedValueBytes.length);
        }

        ctx.length((long)bytes.length).send(bytes);

        new LogMessage.Impl(this.logger, trace, "finish renderValue", LogType.of(success), renderValue, ElapsedFrom.now()).log();
    }

    @Override
    public String name() {
        return "loggingJson";
    }
    //endregion

    //region Fields
    protected Logger logger;
    protected MetricRegistry metricRegistry;

    private static MethodName.MDCWriter renderValue = MethodName.of("renderValue");

    private static byte[] renderElapsedBytes = "renderElapsed".getBytes();
    private static byte[] totalElapsedBytes = "totalElapsed".getBytes();

    private static byte[] sampleValueBytes = String.format("%08d", 0).getBytes();

    private static byte[] columnBytes = ":".getBytes();
    private static byte[] zeroBytes = "0".getBytes();
    //endregion
}
