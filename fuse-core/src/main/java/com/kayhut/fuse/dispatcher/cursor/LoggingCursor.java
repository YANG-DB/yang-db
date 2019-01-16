package com.kayhut.fuse.dispatcher.cursor;

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

import com.codahale.metrics.MetricRegistry;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.model.results.QueryResultBase;
import org.slf4j.Logger;

import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;

/**
 * Created by roman.margolis on 07/01/2018.
 */
public class LoggingCursor implements Cursor {

    public static final String CURSOR = "cursor";

    public static final String CURSOR_COUNT = CURSOR+".count";

    //region Constructors
    public LoggingCursor(Cursor cursor, Logger logger, MetricRegistry metricRegistry) {
        this.cursor = cursor;
        this.logger = logger;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region Cursor Implementation
    @Override
    public QueryResultBase getNextResults(int numResults) {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start getNextResults", sequence, LogType.of(start), getNextResults, ElapsedFrom.now()).log();
            metricRegistry.counter(CURSOR_COUNT).inc(1);
            return this.cursor.getNextResults(numResults);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed getNextResults", sequence, LogType.of(failure), getNextResults, ElapsedFrom.now())
                    .with(ex).log();
            throw ex;
        } finally {
            metricRegistry.counter(CURSOR_COUNT).dec(1);
            if (!thrownException) {
                new LogMessage.Impl(this.logger, trace, "finish getNextResults", sequence, LogType.of(success), getNextResults, ElapsedFrom.now()).log();
            }
        }
    }
    //endregion

    //region Fields
    private Cursor cursor;
    private Logger logger;
    private MetricRegistry metricRegistry;

    private static MethodName.MDCWriter getNextResults = MethodName.of("getNextResults");
    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}
