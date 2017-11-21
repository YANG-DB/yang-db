package com.kayhut.fuse.gta.translation.discrete;

import com.codahale.metrics.Slf4jReporter;
import com.kayhut.fuse.dispatcher.utils.LoggerAnnotation;
import com.kayhut.fuse.gta.strategy.discrete.M1PlanOpTranslationStrategy;
import com.kayhut.fuse.gta.translation.ChainedPlanOpTraversalTranslator;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.Plan;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Element;

/**
 * Created by Roman on 28/06/2017.
 */
public class M1PlanTraversalTranslator extends ChainedPlanOpTraversalTranslator {
    //region Constructors
    public M1PlanTraversalTranslator() {
        super(new M1PlanOpTranslationStrategy());
    }
    //endregion

    //region Override Methods
    @Override
    @LoggerAnnotation(name = "translate", options = LoggerAnnotation.Options.returnValue, logLevel = Slf4jReporter.LoggingLevel.INFO)
    public GraphTraversal<?, ?> translate(Plan plan, TranslationContext context) {
        return super.translate(plan, context);
    }
    //endregion
}
