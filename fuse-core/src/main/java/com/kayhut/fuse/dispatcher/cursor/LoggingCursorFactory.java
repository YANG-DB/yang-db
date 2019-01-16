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
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;

/**
 * Created by roman.margolis on 07/01/2018.
 */
public class LoggingCursorFactory implements CursorFactory {
    public static final String cursorFactoryParameter = "LoggingCursorFactory.@cursorFactory";
    public static final String loggerParameter = "LoggingCursorFactory.@logger";

    //region Constructors
    @Inject
    public LoggingCursorFactory(
            MetricRegistry metricRegistry,
            @Named(cursorFactoryParameter) CursorFactory cursorFactory,
            @Named(loggerParameter) Logger logger) {
        this.metricRegistry = metricRegistry;
        this.cursorFactory = cursorFactory;
        this.logger = logger;
    }
    //endregion

    //region LoggingFactory Implementation
    @Override
    public Cursor createCursor(Context context) {
        return new LoggingCursor(this.cursorFactory.createCursor(context), this.logger, metricRegistry);
    }
    //endregion

    private MetricRegistry metricRegistry;
    //region Fields
    private CursorFactory cursorFactory;
    private Logger logger;
    //endregion
}
