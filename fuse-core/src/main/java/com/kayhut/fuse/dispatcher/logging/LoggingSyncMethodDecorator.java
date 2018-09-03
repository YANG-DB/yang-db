package com.kayhut.fuse.dispatcher.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.kayhut.fuse.dispatcher.decorators.MethodDecorator;
import com.kayhut.fuse.dispatcher.logging.LogMessage.MDCWriter.Noop;
import com.kayhut.fuse.model.transport.ContentResponse;
import javaslang.collection.Stream;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.kayhut.fuse.dispatcher.logging.LogType.failure;
import static com.kayhut.fuse.dispatcher.logging.LogType.start;
import static com.kayhut.fuse.dispatcher.logging.LogType.success;

public class LoggingSyncMethodDecorator<TResult> implements MethodDecorator<TResult, Long> {
    //region Constructors
    public LoggingSyncMethodDecorator(
            Logger logger,
            MetricRegistry metricRegistry,
            MethodName.MDCWriter methodName,
            LogMessage.Level level) {
        this(logger, metricRegistry, methodName, Noop.instance, Collections.singletonList(level), Collections.singletonList(level));
    }

    public LoggingSyncMethodDecorator(
            Logger logger,
            MetricRegistry metricRegistry,
            MethodName.MDCWriter methodName,
            LogMessage.MDCWriter primerMdcWriter,
            LogMessage.Level level) {
        this(logger, metricRegistry, methodName, primerMdcWriter, Collections.singletonList(level), Collections.singletonList(level));
    }

    public LoggingSyncMethodDecorator(
            Logger logger,
            MetricRegistry metricRegistry,
            MethodName.MDCWriter methodName,
            LogMessage.MDCWriter primerMdcWriter,
            Iterable<LogMessage.Level> preInvocationLevels,
            Iterable<LogMessage.Level> onSuccessLevels) {
        this.logger = logger;
        this.metricRegistry = metricRegistry;
        this.methodName = methodName;
        this.primerMdcWriter = primerMdcWriter;

        this.preInvocationLevels = preInvocationLevels;
        this.onSuccessLevels = onSuccessLevels;
    }
    //endregion

    //region MethodDecorator Implementation
    @Override
    public TResult decorate(Supplier<TResult> methodInvocationSupplier, ResultHandler<TResult, Long> resultHandler) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), this.methodName.toString())).time();

        this.primerMdcWriter.write();

        try {
            Stream.ofAll(this.preInvocationLevels).forEach(level -> {
                        new LogMessage.Impl(this.logger, level, String.format("start %s", methodName.toString()),
                                sequence, ElapsedFrom.now(), LogType.of(start), this.methodName)
                                .log();
                    });

            TResult methodInvocationResult = methodInvocationSupplier.get();

            Stream.ofAll(this.onSuccessLevels).forEach(level -> {
                new LogMessage.Impl(this.logger, level, String.format("finish %s", methodName.toString()),
                        sequence, ElapsedFrom.now(), LogType.of(success), this.methodName)
                        .log();
            });

            this.metricRegistry.meter(name(this.logger.getName(), this.methodName.toString(), "success")).mark();
            return resultHandler.onSuccess(methodInvocationResult, TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS));
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, String.format("failed %s", methodName.toString()),
                    sequence, ElapsedFrom.now(), LogType.of(failure), this.methodName)
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), this.methodName.toString(), "failure")).mark();
            return resultHandler.onFailure(ex, TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS));
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private MethodName.MDCWriter methodName;

    private LogMessage.MDCWriter primerMdcWriter;

    private Iterable<LogMessage.Level> preInvocationLevels;
    private Iterable<LogMessage.Level> onSuccessLevels;

    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}
