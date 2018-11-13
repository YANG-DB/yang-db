package com.kayhut.fuse.dispatcher.epb;

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
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import org.slf4j.Logger;

import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.*;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class LoggingPlanSearcher<P, C, Q> implements PlanSearcher<P, C, Q> {
    public static final String planSearcherParameter = "LoggingPlanSearcher.@descriptor";
    public static final String descriptorParameter = "LoggingPlanSearcher.@planSearcher";
    public static final String loggerParameter = "LoggingPlanSearcher.@logger";

    //region Constructors
    @Inject
    public LoggingPlanSearcher(
            @Named(planSearcherParameter) PlanSearcher<P, C, Q> planSearcher,
            Descriptor<PlanWithCost<P, C>> descriptor,
            @Named(loggerParameter) Logger logger,
            MetricRegistry metricRegistry) {
        this.logger = logger;
        this.metricRegistry = metricRegistry;
        this.planSearcher = planSearcher;
        this.descriptor = descriptor;
    }
    //endregion

    //region PlanSearcher Implementation
    @Override
    public PlanWithCost<P, C> search(Q query) {
        return new LoggingSyncMethodDecorator<PlanWithCost<P, C>>(this.logger, this.metricRegistry, search, trace)
                .decorate(() -> {
                    PlanWithCost<P, C> planWithCost = this.planSearcher.search(query);
                    if (planWithCost != null) {
                        new LogMessage.Impl(this.logger, debug, "execution plan: {}", sequence, LogType.of(log), search, ElapsedFrom.now())
                                .with(this.descriptor.describe(planWithCost)).log();
                    }
                    return planWithCost;
                });
    }
    //endregion

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private PlanSearcher<P, C, Q> planSearcher;
    private Descriptor<PlanWithCost<P, C>> descriptor;

    private static MethodName.MDCWriter search = MethodName.of("search");
    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}
