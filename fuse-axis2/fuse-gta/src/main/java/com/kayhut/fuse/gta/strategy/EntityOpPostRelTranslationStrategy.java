package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import javaslang.Tuple2;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

/**
 * Created by benishue on 08-Mar-17.
 */
public class EntityOpPostRelTranslationStrategy implements TranslationStrategy {

    @Override
    public GraphTraversal apply(Tuple2<Plan, PlanOpBase> context, GraphTraversal traversal) {
        Plan plan = context._1;
        PlanOpBase currentPlanOpBase = context._2;
        Optional<PlanOpBase> prev = plan.getPrev(currentPlanOpBase);
        if(!prev.isPresent())
            return traversal;

        //EntityOp Post RelOp
        if(currentPlanOpBase instanceof EntityOp && prev.get() instanceof RelationOp) {
            traversal.otherV().as(((EntityOp)currentPlanOpBase).getEntity().geteBase().geteTag());
        }
        return traversal;

    }
}
