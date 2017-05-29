package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

/**
 * Created by Roman on 24/05/2017.
 */
public abstract class PlanOpTranslationStrategyBase implements PlanOpTranslationStrategy {
    //region Constructors
    @SafeVarargs
    public PlanOpTranslationStrategyBase(Class<? extends PlanOpBase>...klasses) {
        this.klasses = klasses;
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    public GraphTraversal translate(GraphTraversal traversal, Plan plan, PlanOpBase planOp, TranslationContext context) {
        if (Stream.of(klasses).filter(klass -> klass.isAssignableFrom(planOp.getClass())).isEmpty()) {
            return traversal;
        }

        return translateImpl(traversal, plan, planOp, context);
    }
    //endregion

    //region Abstract Methods
    protected abstract GraphTraversal translateImpl(GraphTraversal traversal, Plan plan, PlanOpBase planOp, TranslationContext context);
    //endregion

    //region Fields
    private Class<? extends PlanOpBase>[] klasses;
    //endregion
}
