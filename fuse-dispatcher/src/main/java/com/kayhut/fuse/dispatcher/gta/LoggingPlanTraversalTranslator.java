package com.kayhut.fuse.dispatcher.gta;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class LoggingPlanTraversalTranslator implements PlanTraversalTranslator {
    public static final String injectionName = "LoggingPlanTraversalTranslator.inner";

    //region Constructors
    @Inject
    public LoggingPlanTraversalTranslator(@Named(injectionName) PlanTraversalTranslator innerTranslator, Descriptor<GraphTraversal<?, ?>> descriptor) {
        this.logger = LoggerFactory.getLogger(innerTranslator.getClass());
        this.innerTranslator = innerTranslator;
        this.descriptor = descriptor;
    }
    //endregion

    //region PlanTraversalTranslator
    @Override
    public GraphTraversal<?, ?> translate(PlanWithCost<Plan, PlanDetailedCost> planWithCost, TranslationContext context) {
        boolean thrownException = false;

        try {
            this.logger.trace("start translate");
            GraphTraversal<?, ?> traversal = this.innerTranslator.translate(planWithCost, context);
            this.logger.debug("traversal: {}", this.descriptor.describe(traversal));
            return traversal;
        } catch (Exception ex) {
            this.logger.error("failed translate: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish translate");
            }
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private PlanTraversalTranslator innerTranslator;
    private Descriptor<GraphTraversal<?, ?>> descriptor;
    //endregion
}
