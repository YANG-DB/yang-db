package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.unipop.Constraint;
import javaslang.Tuple2;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.util.Optional;

/**
 * Created by benishue on 08-Mar-17.
 *
 *
 *
 * relOp = outE('promise').has('constraint', P.eq(Constraint.by(__.and(
 *                  __.has('label', P.eq(<Ontology(<rel.rType>)>)),
 *                  __.has('direction', P.eq(TinkerPop.direction(<Rel.dir>)))))))
 *                  .as(source(<EEntityBase.Etag>)-->target(<EEntityBase.Etag></EEntityBase.Etag>))  // A-->B
 *
 */
public class RelationOpTranslationStrategy implements TranslationStrategy {



    @Override
    public GraphTraversal apply(Tuple2<Plan, PlanOpBase> context, GraphTraversal traversal) {
        Plan plan = context._1;
        PlanOpBase planOpBase = context._2;
        Optional<PlanOpBase> prev = plan.getPrev(planOpBase);

        if(planOpBase instanceof RelationOp) {
            Rel rel = ((RelationOp) planOpBase).getRelation().geteBase();
            traversal.outE("promise").has("constraint", P.eq(Constraint.by(__.and(
                   __.has("label",P.eq(rel.getrType())),
                   __.has("direction", P.eq(getTinkerPopDirection(rel.getDir())))
            ))));
        }
        return traversal;

    }


    private String getTinkerPopDirection(String dir) {
        String tinkerPopDirection;
        switch (dir) {
            case "R":
                tinkerPopDirection = "out";
                break;
            case "L":
                tinkerPopDirection = "in";
                break;
            default:
                throw new IllegalArgumentException("Not Supported Direction: " + dir);
        }
        return tinkerPopDirection;
    }
}
