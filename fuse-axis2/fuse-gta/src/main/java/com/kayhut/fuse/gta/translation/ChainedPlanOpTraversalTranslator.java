package com.kayhut.fuse.gta.translation;

import com.google.inject.Inject;
import com.kayhut.fuse.gta.strategy.*;
import com.kayhut.fuse.model.execution.plan.*;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;

/**
 * Created by moti on 3/7/2017.
 */
public class ChainedPlanOpTraversalTranslator implements PlanTraversalTranslator {
    //region Constructors
    @Inject
    public ChainedPlanOpTraversalTranslator(PlanOpTranslationStrategy translationStrategy) {
        this.translationStrategy = translationStrategy;
    }
    //endregion

    //region PlanTraversalTranslator Implementation
    public Traversal<Element, Path> translate(Plan plan, TranslationContext context) throws Exception {
        GraphTraversal traversal = __.start();
        for (PlanOpBase planOp : plan.getOps()) {
            traversal = this.translationStrategy.translate(traversal, plan, planOp, context);
        }

        return traversal.path();
    }
    //endregion

    //region Fields
    private PlanOpTranslationStrategy translationStrategy;
    //endregion
}
