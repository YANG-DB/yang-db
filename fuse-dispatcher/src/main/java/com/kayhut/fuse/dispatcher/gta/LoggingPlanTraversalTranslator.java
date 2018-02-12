package com.kayhut.fuse.dispatcher.gta;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.ElapsedFrom;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import com.kayhut.fuse.dispatcher.logging.LogType;
import com.kayhut.fuse.dispatcher.logging.MethodName;
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
import static com.kayhut.fuse.dispatcher.logging.LogType.*;

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
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), translate.toString())).time();

        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start translate", LogType.of(start), translate, ElapsedFrom.now()).log();
            GraphTraversal<?, ?> traversal = this.innerTranslator.translate(planWithCost, context);
            //TODO: performance bug: The descriptor takes a very long time to render
            /*new LogMessage.Impl(this.logger, debug, "traversal: {}", LogType.of(log), translate, ElapsedFrom.now())
                    .with(this.descriptor.describe(traversal)).log();*/
            return traversal;
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed translate", LogType.of(failure), translate, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), translate.toString(), "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, trace, "finish translate", LogType.of(success), translate, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), translate.toString(), "success")).mark();
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

    private static MethodName.MDCWriter translate = MethodName.of("translate");
    //endregion
}
