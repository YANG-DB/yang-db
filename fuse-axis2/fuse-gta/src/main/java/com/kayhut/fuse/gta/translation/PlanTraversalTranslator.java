package com.kayhut.fuse.gta.translation;

import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 10/05/2017.
 */
public interface PlanTraversalTranslator {
    GraphTraversal<?, ?> translate(PlanWithCost<Plan, PlanDetailedCost> planWithCost, TranslationContext context);
}
