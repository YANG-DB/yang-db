package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

/**
 * Created by Roman on 24/05/2017.
 */
public abstract class PlanOpTranslationStrategyBase<T extends PlanOpBase> implements PlanOpTranslationStrategy {
    //region Constructors
    public PlanOpTranslationStrategyBase(Class<T> klass) {
        this.klass = klass;
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    public GraphTraversal translate(GraphTraversal traversal, Plan plan, PlanOpBase planOp, TranslationContext context) {
        if (!klass.isAssignableFrom(planOp.getClass())) {
            return traversal;
        }

        return translateImpl(traversal, plan, (T)planOp, context);
    }
    //endregion

    //region Abstract Methods
    protected abstract GraphTraversal translateImpl(GraphTraversal traversal, Plan plan, T planOp, TranslationContext context);
    //endregion

    //region Fields
    private Class<T> klass;
    //endregion
}
