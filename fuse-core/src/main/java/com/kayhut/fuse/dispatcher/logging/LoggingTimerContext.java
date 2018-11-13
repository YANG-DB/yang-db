package com.kayhut.fuse.dispatcher.logging;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2018 kayhut
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
