package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import javaslang.Tuple2;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

/**
 * Created by benishue on 08-Mar-17.
 *
 *
 * EEntityBase = select(<EntityBase.Etag>)
 */
public class EntityOpAdjcentTranslationStrategy implements TranslationStrategy {

    @Override
    public GraphTraversal apply(TranslationStrategyContext context, GraphTraversal traversal) {
        Plan plan = context.getPlan();
        PlanOpBase currentPlanOpBase = context.getPlanOpBase();
        PlanUtil planUtil = new PlanUtil();
        Optional<PlanOpBase> prev = planUtil.getPrev(plan.getOps(), currentPlanOpBase);
        if(!prev.isPresent())
            return traversal;

        if(currentPlanOpBase instanceof EntityOp && prev.get() instanceof EntityOp) {
            traversal.select(((EntityOp) currentPlanOpBase).getEntity().geteBase().geteTag());
        }

        return traversal;
    }
}
