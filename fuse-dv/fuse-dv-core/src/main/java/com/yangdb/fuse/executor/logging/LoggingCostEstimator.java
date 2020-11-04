package com.yangdb.fuse.executor.logging;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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
import com.yangdb.fuse.dispatcher.epb.CostEstimator;
import com.yangdb.fuse.dispatcher.epb.CostEstimatorDriver;
import com.yangdb.fuse.dispatcher.logging.*;
import com.yangdb.fuse.model.descriptors.Descriptor;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.slf4j.Logger;
import org.unipop.process.Profiler;

import static com.yangdb.fuse.dispatcher.logging.LogMessage.Level.debug;
import static com.yangdb.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.yangdb.fuse.dispatcher.logging.LogType.log;
import static com.yangdb.fuse.dispatcher.logging.LogType.metric;
import static org.unipop.process.Profiler.PROFILER;

public class LoggingCostEstimator<P, C, Q> implements CostEstimatorDriver<P, C, Q, GraphTraversal<?, ?>> {
    public static final String planSearcherParameter = "LoggingCostEstimator.@descriptor";
    public static final String loggerParameter = "LoggingCostEstimator.@logger";

    //region Constructors
    @Inject
    public LoggingCostEstimator(
            @Named(planSearcherParameter) CostEstimatorDriver<P, C, Q,GraphTraversal<?, ?>> costEstimator,
            @Named(loggerParameter) Logger logger,
            MetricRegistry metricRegistry) {
        this.logger = logger;
        this.metricRegistry = metricRegistry;
        this.costEstimator = costEstimator;
    }
    //endregion

    @Override
    public PlanWithCost<P, C> estimate(P plan, Q q) {
        return new LoggingSyncMethodDecorator<PlanWithCost<P, C>>(this.logger, this.metricRegistry, estimate, trace)
                .decorate(() -> {
                    PlanWithCost<P, C> planWithCost = this.costEstimator.estimate(plan, q);
                    if (planWithCost != null) {
                        new LogMessage.Impl(this.logger, debug, "cost estimation plan: {}", sequence, LogType.of(log), estimate, ElapsedFrom.now()).log();
                    }
                    return planWithCost;
                });
    }

    @Override
    public Long count(GraphTraversal<?, ?> driver) {
        return new LoggingSyncMethodDecorator<Long>(this.logger, this.metricRegistry, estimate, trace)
                .decorate(() -> this.costEstimator.count(driver));

    }
    //endregion

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private CostEstimatorDriver<P, C, Q,GraphTraversal<?, ?>> costEstimator;

    private static LogMessage.MDCWriter profile = MethodName.of("profile");
    private static MethodName.MDCWriter estimate = MethodName.of("estimate");
    private static LogMessage.MDCWriter sequence = Sequence.incr();

    //endregion
}
