package com.kayhut.fuse.dispatcher.gta;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.debug;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.LogType.*;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.LogType.start;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class LoggingPlanTraversalTranslator implements PlanTraversalTranslator {
    public static final String planTraversalTranslatorParameter = "LoggingPlanTraversalTranslator.@planTraversalTranslator";
    public static final String loggerParameter = "LoggingPlanTraversalTranslator.@logger";

    //region Constructors
    @Inject
    public LoggingPlanTraversalTranslator(
            @Named(planTraversalTranslatorParameter) PlanTraversalTranslator planTraversalTranslator,
            Descriptor<GraphTraversal<?, ?>> descriptor,
            @Named(loggerParameter) Logger logger,
            MetricRegistry metricRegistry) {
        this.logger = logger;
        this.metricRegistry = metricRegistry;
        this.innerTranslator = planTraversalTranslator;
        this.descriptor = descriptor;
    }
    //endregion

    //region PlanTraversalTranslator
    @Override
    public GraphTraversal<?, ?> translate(PlanWithCost<Plan, PlanDetailedCost> planWithCost, TranslationContext context) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "translate")).time();

        boolean thrownException = false;

        try {
            new LogMessage(this.logger, trace, start, "translate", "start translate").log();
            GraphTraversal<?, ?> traversal = this.innerTranslator.translate(planWithCost, context);
            new LogMessage(this.logger, debug, log, "translate", "traversal: {}", this.descriptor.describe(traversal)).log();
            return traversal;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage(this.logger, error, finish, "translate", "failed translate", ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), "translate", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage(this.logger, trace, finish, "translate", "finish translate").log();
                this.metricRegistry.meter(name(this.logger.getName(), "translate", "success")).mark();
            }
            timerContext.stop();
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private PlanTraversalTranslator innerTranslator;
    private Descriptor<GraphTraversal<?, ?>> descriptor;
    //endregion
}
