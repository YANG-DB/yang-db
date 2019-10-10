package com.yangdb.fuse.executor.logging;

/*-
 *
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.dispatcher.cursor.LoggingCursor;
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import org.slf4j.Logger;

/**
 * Created by roman.margolis on 07/01/2018.
 */
public class LoggingCursorFactory implements CursorFactory {
    public static final String cursorFactoryParameter = "LoggingCursorFactory.@cursorFactory";
    public static final String cursorLoggerParameter = "LoggingCursorFactory.@cursorLogger";
    public static final String traversalLoggerParameter = "LoggingCursorFactory.@traversalLogger";

    //region Constructors
    @Inject
    public LoggingCursorFactory(
            MetricRegistry metricRegistry,
            @Named(cursorFactoryParameter) CursorFactory cursorFactory,
            @Named(cursorLoggerParameter) Logger cursorLogger,
            @Named(traversalLoggerParameter) Logger traversalLogger) {
        this.metricRegistry = metricRegistry;
        this.cursorFactory = cursorFactory;
        this.cursorLogger = cursorLogger;
        this.traversalLogger = traversalLogger;
    }
    //endregion

    //region LoggingFactory Implementation
    @Override
    public Cursor createCursor(Context context) {
        TraversalCursorContext traversalCursorContext = (TraversalCursorContext)context;
        TraversalCursorContext loggingTraversalCursorContext = traversalCursorContext.clone();
        loggingTraversalCursorContext.setTraversal(new LoggingTraversal<>(traversalCursorContext.getTraversal(), this.traversalLogger));
        return new LoggingCursor(this.cursorFactory.createCursor(loggingTraversalCursorContext), this.cursorLogger, metricRegistry);
    }
    //endregion

    private MetricRegistry metricRegistry;
    //region Fields
    private CursorFactory cursorFactory;
    private Logger cursorLogger;
    private Logger traversalLogger;
    //endregion
}
