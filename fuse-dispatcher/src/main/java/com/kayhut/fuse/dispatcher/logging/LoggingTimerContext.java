package com.kayhut.fuse.dispatcher.logging;

import com.codahale.metrics.Timer;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LoggingTimerContext implements Closeable {
    //region Constructors
    public LoggingTimerContext(Timer.Context timerContext, Consumer<Long> elapsedConsumer) {
        this.timerContext = timerContext;
        this.elapsedConsumer = elapsedConsumer;
    }
    //endregion

    //region Closeable Implementation
    @Override
    public void close() throws IOException {
        long elapsedMilliseconds = TimeUnit.MILLISECONDS.convert(timerContext.stop(), TimeUnit.NANOSECONDS);
        this.elapsedConsumer.accept(elapsedMilliseconds);
    }
    //endregion

    //region Fields
    private Timer.Context timerContext;
    private Consumer<Long> elapsedConsumer;
    //endregion
}
