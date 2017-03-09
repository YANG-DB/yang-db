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
public class RelationOpTranslationStrategy implements TranslationStrategy {

    @Override
    public GraphTraversal apply(Tuple2<Plan, PlanOpBase> context, GraphTraversal traversal) {
        Plan plan = context._1;
        PlanOpBase planOpBase = context._2;
        if(planOpBase instanceof RelationOp) {
            //todo ask Roman what should outE get?
            traversal.outE();
        }
        return traversal;

    }
}
