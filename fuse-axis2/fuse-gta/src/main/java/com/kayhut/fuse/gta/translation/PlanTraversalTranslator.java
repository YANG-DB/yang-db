package com.kayhut.fuse.gta.translation;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Element;

/**
 * Created by Roman on 10/05/2017.
 */
public interface PlanTraversalTranslator {
    Traversal<Element, Path> translate(PlanWithCost<Plan, PlanDetailedCost> plan, TranslationContext context) throws Exception;
}
