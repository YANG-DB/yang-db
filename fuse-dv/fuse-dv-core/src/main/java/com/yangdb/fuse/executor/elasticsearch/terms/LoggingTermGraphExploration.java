package com.yangdb.fuse.executor.elasticsearch.terms;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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
import com.yangdb.fuse.dispatcher.logging.LoggingSyncMethodDecorator;
import com.yangdb.fuse.dispatcher.logging.MethodName;
import com.yangdb.fuse.executor.elasticsearch.terms.transport.GraphExploreRequest;
import com.yangdb.fuse.executor.elasticsearch.terms.transport.GraphExploreResponse;
import org.elasticsearch.action.ActionListener;
import org.slf4j.Logger;

import static com.yangdb.fuse.dispatcher.logging.LogMessage.Level.trace;

public class LoggingTermGraphExploration implements TermGraphExploration {
    public static final String clientParam = "LoggingTermGraphExploration.@client";
    public static final String loggerParameter = "LoggingTermGraphExploration.@logger";

    //region Constructors
    @Inject
    public LoggingTermGraphExploration(
            @Named(clientParam) TermGraphExploration graphExploration,
            @Named(loggerParameter) Logger logger,
            MetricRegistry metricRegistry) {
        this.graphExploration = graphExploration;
        this.logger = logger;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void doExecute(GraphExploreRequest request, ActionListener<GraphExploreResponse> listener) {
        new LoggingSyncMethodDecorator<>(this.logger, this.metricRegistry, getMapping, trace)
                .decorate(() -> {
                    this.graphExploration.doExecute(request, listener);
                    return null;
                });

    }

    @Override
    public GraphExploreResponse execute(GraphExploreRequest request) {
        return new LoggingSyncMethodDecorator<GraphExploreResponse>(this.logger, this.metricRegistry, getMapping, trace)
                .decorate(() -> this.graphExploration.execute(request));
    }

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private TermGraphExploration graphExploration;

    private static MethodName.MDCWriter search = MethodName.of("search");
    private static MethodName.MDCWriter getMapping = MethodName.of("getMapping");
    private static MethodName.MDCWriter cleanup = MethodName.of("cleanup");
    private static MethodName.MDCWriter schedule = MethodName.of("schedule");


    //endregion

}
