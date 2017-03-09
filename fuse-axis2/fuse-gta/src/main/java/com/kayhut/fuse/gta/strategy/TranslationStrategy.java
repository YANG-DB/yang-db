package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import javaslang.Tuple2;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by benishue on 08-Mar-17.
 */
public interface TranslationStrategy {
    /**
     * traversal returns same instance as in parameter
     * @param context
     * @param traversal - is mutated parameter
     * @return
     */
    GraphTraversal apply(Tuple2<Plan, PlanOpBase> context, GraphTraversal traversal);
}
